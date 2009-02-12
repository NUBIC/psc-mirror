require "buildr"
require "buildr/jetty"

###### buildr script for PSC
# In order to use this, you'll need buildr.  See http://buildr.apache.org/ .

VERSION_NUMBER="2.5-SNAPSHOT"
APPLICATION_SHORT_NAME = 'psc'


###### Jetty config

Java.classpath << [
  "org.mortbay.jetty:jsp-api-2.1:jar:#{Buildr::Jetty::VERSION}",
  "org.mortbay.jetty:jsp-2.1:jar:#{Buildr::Jetty::VERSION}"
]
jetty.url = "http://localhost:7200"

###### PROJECT

desc "Patient Study Calendar"
define "psc" do
  project.version = VERSION_NUMBER
  project.group = "edu.northwestern.bioinformatics.studycalendar"

  # resources.from(_("src/main/java")).exclude("**/*.java")
  compile.options.target = "1.5"
  compile.options.source = "1.5"
  compile.options.other = %w(-encoding UTF-8)
  # compile.with CTMS_COMMONS, CORE_COMMONS, SECURITY, XML, SPRING, HIBERNATE, 
  #   LOGBACK, SLF4J, JAKARTA_COMMONS, CAGRID, BERING, WEB, DB, CONTAINER_PROVIDED
  
  # test.resources.from(_("src/test/java")).exclude("**/*.java")
  # test.with(UNIT_TESTING, 'psc:test-infrastructure').include("*Test")

  # package(:war).exclude(CONTAINER_PROVIDED)
  # package(:sources)
  
  # resources task(:init)
  
  # db = ENV['DB'] || 'studycalendar'
  # dbprops = { } # Filled in by :init
  
  # test.resources task(:test_csm_config)
  
  # task :test_csm_config => :init do
  #   filter(_("conf/upt")).include('*.xml').into(_("target/test-classes")).
  #     using(:ant, {'tomcat.security.dir' => _("target/test-classes")}.merge(dbprops)).run
  # end

  task :public_demo_deploy do
    cp FileList[_("test/public/*")], "/opt/tomcat/webapps-vera/studycalendar/"
  end
  
  define "Pure utility code"
  define "utility" do
    resources.from(_("src/main/java")).exclude("**/*.java")
    test.resources.from(_("src/test/java")).exclude("**/*.java")
    
    compile.with SLF4J, SPRING, JAKARTA_COMMONS.collections, 
      JAKARTA_COMMONS.collections_generic, CTMS_COMMONS.lang
    test.with(UNIT_TESTING)
    
    package(:jar)
    package(:sources)
  end
  
  desc "The domain classes for PSC"
  define "domain" do
    resources.from(_("src/main/java")).exclude("**/*.java")
    test.resources.from(_("src/test/java")).exclude("**/*.java")
    
    compile.with project('utility'), SLF4J, CTMS_COMMONS, CORE_COMMONS, 
      JAKARTA_COMMONS, SPRING, HIBERNATE, SECURITY
    test.with(UNIT_TESTING)
    
    package(:jar)
    package(:sources)
  end
  
  desc "Core data access, serialization and non-substitutable business logic"
  define "core" do
    task :refilter do
      rm_rf Dir[_(resources.target.to_s, "applicationContext-{spring,setup}.xml")]
    end
    resources.enhance [:refilter]
    
    def filter_tokens
      {
        'application-short-name'  => APPLICATION_SHORT_NAME,
        'config.database'         => db_name,
        "buildInfo.versionNumber" => project.version,
        "buildInfo.username"      => ENV['USER'],
        "buildInfo.hostname"      => `hostname`.chomp,
        "buildInfo.timestamp"     => Time.now.strftime("%Y-%m-%d %H:%M:%S")
      }
    end
    
    # :ant filtering, but with deferred token creation so that the db name
    # can be influenced by other tasks
    resources.from(_("src/main/java")).exclude("**/*.java").filter.using do |path, content|
      deferred_tokens = filter_tokens
      content.gsub(/@.*?@/) do |key|
        deferred_tokens[key[1..-2]] || key
      end
    end
    
    # Migrations are resources, too
    resources.enhance do
      filter.from(_("src/main/db/migrate")).
        into(resources.target.to_s + "/db/migrate").run
    end

    compile.with project('domain'), project('domain').compile.dependencies, 
      BERING, DB, XML, RESTLET.framework, FREEMARKER, CSV, CONTAINER_PROVIDED,
      QUARTZ, 
      SPRING_WEB # tmp for mail

    test.with UNIT_TESTING, project('domain').test.compile.target
    test.resources.from(_("src/test/java")).exclude("**/*.java")

    # Automatically generate the HSQLDB when the migrations change
    # if using hsqldb.
    if db_name =~ /hsqldb/
      test.enhance hsqldb[:files]
      hsqldb[:files].each do |f|
        file(f => Dir[_('src/main/db/migrate/**/*')]) do
          task(:create_hsqldb).invoke
        end
      end
    end
    
    package(:jar)
    package(:sources)
    
    check do
      acSpring = File.read(_('target/resources/applicationContext-spring.xml'))
      acSetup = File.read(_('target/resources/applicationContext-setup.xml'))
      
      acSpring.should include(filter_tokens['config.database'])
      acSetup.should include(filter_tokens['buildInfo.hostname'])
      acSetup.should include(project.version)
    end
    
    task :migrate do
      ant('bering') do |ant|
        # Load DS properties from /etc/psc or ~/.psc
        datasource_properties(ant)
        ant.echo :message => "Migrating ${datasource.url}"

        # default values
        ant.property :name => 'migrate.version', :value => ENV['MIGRATE_VERSION'] || ""
        ant.property :name => 'bering.dialect', :value => ""
        
        ant.taskdef :resource => "edu/northwestern/bioinformatics/bering/antlib.xml",
          :classpath => ant_classpath(project('core'))
        ant.migrate :driver => '${datasource.driver}',
          :dialect => "${bering.dialect}",
          :url => "${datasource.url}",
          :userid => "${datasource.username}",
          :password => "${datasource.password}",
          :targetVersion => "${migrate.version}",
          :migrationsDir => _("src/main/db/migrate"),
          :classpath => ant_classpath(project('core'))
      end
    end

    task :create_hsqldb do |t|
      rm_rf hsqldb[:dir]
      
      File.open("#{ENV['HOME']}/.psc/#{db_name}.properties", 'w') do |f|
        f.puts( (<<-PROPERTIES).split(/\n/).collect { |row| row.strip }.join("\n") )
          # Generated by PSC's psc:core:create_hsqldb task
          datasource.url=#{hsqldb[:url]}
          datasource.username=sa
          datasource.password=
          datasource.driver=org.hsqldb.jdbcDriver
        PROPERTIES
      end
      
      # Apply the bering migrations to build the HSQLDB schema
      mkdir hsqldb[:dir]
      task(:migrate).invoke
      
      # Explicit shutdown required to allow other processes to open the database
      ant('sql') do |ant|
        ant.sql(
          :driver => "org.hsqldb.jdbcDriver", :url => hsqldb[:url],
          :userid => "sa", :password => "", 
          :classpath => ant_classpath(project('core')), 
          :autocommit => "true",
          :pcdata => "SHUTDOWN SCRIPT;")
      end
      
      # Mark read-only
      File.open("#{hsqldb[:dir]}/#{db_name}.properties", 'a') do |f|
        f.puts "hsqldb.files_readonly=true"
      end
      
      puts "Read-only HSQLB instance named #{db_name} generated in #{hsqldb[:dir]}"
    end
  end # core
  
  desc "Web interfaces, including the GUI and the RESTful API"
  define "web" do
    resources.from(_("src/main/java")).exclude("**/*.java")
    test.resources.from(_("src/test/java")).exclude("**/*.java")
    
    compile.with LOGBACK, 
      project('core'), project('core').compile.dependencies, 
      SPRING_WEB, RESTLET, WEB, CAGRID

    test.with project('test-infrastructure'), 
      project('test-infrastructure').compile.dependencies,
      project('test-infrastructure').test.compile.dependencies

    package(:war, :file => _('target/psc.war')).tap do |war|
      war.libs -= artifacts(CONTAINER_PROVIDED)
      war.libs -= war.libs.select { |artifact| artifact.classifier == 'sources' }
    end
    package(:sources)
    
    directory(_('src/main/webapp/WEB-INF/lib'))
    
    task :explode => [compile, _('src/main/webapp/WEB-INF/lib')] do
      packages.detect { |pkg| pkg.to_s =~ /war$/ }.tap do |war_package|
        war_package.classes.each do |clz_src|
          filter.from(clz_src).into(_('src/main/webapp/WEB-INF/classes')).run
        end
        war_package.libs.each do |lib|
          cp lib.to_s, _('src/main/webapp/WEB-INF/lib')
        end
      end
    end
    
    # clean exploded files, too
    clean { 
      rm_rf _('src/main/webapp/WEB-INF/lib')
      rm_rf _('src/main/webapp/WEB-INF/classes') 
    }
  end
  
  desc "Common test code for both the module unit tests and the integrated tests"
  define "test-infrastructure", :base_dir => _('test/infrastructure') do
    resources.from(_("src/main/java")).exclude("**/*.java")
    test.resources.from(_("src/test/java")).exclude("**/*.java")

    compile.with UNIT_TESTING, INTEGRATED_TESTING, SPRING_WEB,
      project('core'), project('core').compile.dependencies
    test.with project('core').test.compile.target, 
      project('core').test.compile.dependencies
    package(:jar)
    package(:sources)
  end
end

###### Shared configuration

projects.each do |p|
  if File.exist?(p._("src/main/java"))
    # This doesn't work with the test task for some reason
    # All resources come from source path
    # p.resources.from(p._("src/main/java")).exclude("**/*.java")
    # p.test.resources.from(p._("src/test/java")).exclude("**/*.java")
  
    # Use same logback test config for all modules
    logback_test_src = project("psc")._("src/test/resources/logback-test.xml")
    logback_test_dst = File.join(p.test.resources.target.to_s, "logback-test.xml")
    file logback_test_dst => logback_test_src do |t|
      filter.clear.from(project("psc")._("src/test/resources")).
        include("logback-test.xml").
        into(p.test.resources.target.to_s).
        using(:project_root => p._).
        run
    end
    p.test.resources.enhance [logback_test_dst.to_sym]
  end
end

###### Top-level aliases for commonly-used tasks

desc "Update the core database schema via bering.  Override the target version with MIGRATE_VERSION=X-Y."
task :migrate => 'psc:core:migrate'

desc "Manually create the HSQLDB instance for unit testing"
task :create_hsqldb => 'psc:core:create_hsqldb'

directory project("psc")._('tmp')
file project("psc")._('tmp/csm_jaas.conf') => project("psc")._('tmp') do |t|
  ant('csm') do |ant|
    datasource_properties(ant)
    %w(url username password driver).each do |v|
      ant.filter :token => "datasource.#{v}", :value => "${datasource.#{v}}"
    end
    ant.copy :file => project("psc")._('csm/csm_jaas.config'), :tofile => t.to_s, :filtering => 'yes'
  end
end

desc "Run PSC from #{jetty.url}/psc"
task :server do
  ENV['test'] = 'no'
  ENV['DB'] = 'datasource'
  
  csm_conf = project("psc")._('tmp/csm_jaas.conf')
  rm_rf csm_conf
  task(csm_conf).invoke
  Java.java.lang.System.setProperty(
    'java.security.auth.login.config', 
    csm_conf
  )
  jetty.use.invoke
  
  task('psc:web:explode').invoke
  
  jetty.deploy "#{jetty.url}/psc", 
    project('psc:web')._('src/main/webapp').to_s
  
  msg = "PSC deployed at #{jetty.url}/psc.  Press ^C to stop."
  
  puts "=" * msg.size
  puts msg
  puts "=" * msg.size
  
  # Keep the script running until interrupted
  while(true)
    sleep(1)
  end
end
