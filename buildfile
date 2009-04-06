require "buildr"
require "buildr/jetty"
require "buildr/emma" if emma?

###### buildr script for PSC
# In order to use this, you'll need buildr.  See http://buildr.apache.org/ .

VERSION_NUMBER="2.5.1.DEV"
APPLICATION_SHORT_NAME = 'psc'

###### Jetty config

# enable JSP support in Jetty
Java.classpath.concat([
  "org.mortbay.jetty:jsp-api-2.1:jar:#{Buildr::Jetty::VERSION}",
  "org.mortbay.jetty:jsp-2.1:jar:#{Buildr::Jetty::VERSION}"
])
jetty.url = "http://localhost:7200"

###### PROJECT

desc "Patient Study Calendar"
define "psc" do
  project.version = VERSION_NUMBER
  project.group = "edu.northwestern.bioinformatics"

  compile.options.target = "1.5"
  compile.options.source = "1.5"
  compile.options.other = %w(-encoding UTF-8)
  
  test.using(:properties => { "psc.config.datasource" => db_name })
  test.enhance [:check_module_packages]
  
  ipr.add_component("CompilerConfiguration") do |component|
    component.option :name => 'DEFAULT_COMPILER', :value => 'Javac'
    component.option :name => 'DEPLOY_AFTER_MAKE', :value => '0'
    component.resourceExtensions do |xml|
      xml.entry :name => '.+\.nonexistent'
    end
    component.wildcardResourceExtensions do |xml|
      xml.entry :name => '?*.nonexistent'
    end
  end
  
  task :public_demo_deploy do
    cp FileList[_("test/public/*")], "/opt/tomcat/webapps-vera/studycalendar/"
  end
  
  desc "Pure utility code"
  define "utility" do
    project.bnd.wrap!

    compile.with SLF4J, SPRING, JAKARTA_COMMONS.collections, CTMS_COMMONS.lang
    test.with(UNIT_TESTING)
    
    package(:jar)
    package(:sources)
  end
  
  desc "The domain classes for PSC"
  define "domain" do
    compile.with project('utility'), SLF4J, CTMS_COMMONS, CORE_COMMONS, 
      JAKARTA_COMMONS, SPRING, HIBERNATE, SECURITY
    test.with(UNIT_TESTING)
    
    package(:jar)
    package(:sources)
  end

  desc "Database configuration and testing"
  define "database" do
    # Migrations are resources, too
    resources.enhance do
      filter.from(_("src/main/db/migrate")).
        into(resources.target.to_s + "/db/migrate").run
    end    
    compile.with BERING, SLF4J, SPRING, CORE_COMMONS, CTMS_COMMONS.core,
      JAKARTA_COMMONS, DB, HIBERNATE
    test.with UNIT_TESTING
    
    # Automatically generate the HSQLDB when the migrations change
    # if using hsqldb.
    test.enhance hsqldb[:files]
    hsqldb[:files].each do |f|
      file(f => Dir[_('src/main/db/migrate/**/*')]) do
        if hsqldb?
          task(:create_hsqldb).invoke
        end
      end
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
          :classpath => ant_classpath(project('database'))
        ant.migrate :driver => '${datasource.driver}',
          :dialect => "${bering.dialect}",
          :url => "${datasource.url}",
          :userid => "${datasource.username}",
          :password => "${datasource.password}",
          :targetVersion => "${migrate.version}",
          :migrationsDir => _("src/main/db/migrate"),
          :classpath => ant_classpath(project('database'))
        if hsqldb?
          # database must be explicitly shutdown in HSQLDB >=1.7.2, so that the lock is
          # released and the tests can reopen it
          
          # TODO: this sometimes (why not in all situations?) kills the buildr
          #  process.  Using CHECKPOINT instead also kills the process, 
          #  as do plain SHUTDOWN and SHUTDOWN COMPACT.
          ant.sql :driver => "${datasource.driver}", 
            :url => "${datasource.url}",
            :userid => "${datasource.username}",
            :password => "${datasource.password}",
            :autocommit => "true",
            :classpath => ant_classpath(project('database')),
            :pcdata => "SHUTDOWN SCRIPT;"
        end
      end
    end
    
    task :create_hsqldb => :clean_hsqldb do |t|
       File.open("#{ENV['HOME']}/.psc/#{db_name}.properties", 'w') do |f|
         f.puts( (<<-PROPERTIES).split(/\n/).collect { |row| row.strip }.join("\n") )
           # Generated by PSC's psc:database:create_hsqldb task
           datasource.url=#{hsqldb[:url]}
           datasource.username=sa
           datasource.password=
           datasource.driver=org.hsqldb.jdbcDriver
         PROPERTIES
       end

       # Apply the bering migrations to build the HSQLDB schema
       mkdir_p hsqldb[:dir]
       task(:migrate).invoke

       # Mark read-only
       File.open("#{hsqldb[:dir]}/#{db_name}.properties", 'a') do |f|
         f.puts "hsqldb.files_readonly=true"
       end

       info "Read-only HSQLB instance named #{db_name} generated in #{hsqldb[:dir]}"
    end
    
    task :clean_hsqldb do
       rm_rf hsqldb[:dir]
    end
    
    package(:jar)
    package(:sources)
  end # database
  
  desc "Pluggable authentication definition and included plugins"
  define "authentication" do
    desc "Interfaces and base classes for the pluggable authentication system"
    define "plugin-api" do
      compile.with project('utility'), SLF4J, 
        CONTAINER_PROVIDED, SPRING, SECURITY, CTMS_COMMONS.core, 
        JAKARTA_COMMONS.lang
      test.with(UNIT_TESTING)
      package(:jar)
    end
    
    desc "Authentication using PSC's local CSM instance"
    define "local-plugin" do
      compile.with project('plugin-api').and_dependencies
      test.with project('plugin-api').test_dependencies,
        project('domain').and_dependencies, project('domain').test_dependencies,
        project('database').and_dependencies, project('database').test_dependencies, DB
      test.resources.filter.using(:ant, 'application-short-name'  => APPLICATION_SHORT_NAME)
      package(:jar)
    end
    
    desc "Authentication via an enterprise-wide CAS server"
    define "cas-plugin" do
      compile.with project('plugin-api').and_dependencies
      test.with project('plugin-api').test_dependencies, 
        project('core').and_dependencies
      package(:jar)
    end
    
    desc "Authentication via caGrid's customized version of CAS"
    define "websso-plugin" do
      compile.with project('plugin-api').and_dependencies,
        project('cas-plugin').and_dependencies
      test.with project('plugin-api').test_dependencies,
        project('cas-plugin').test_dependencies, 
        project('domain').and_dependencies, 
        project('domain').test_dependencies
      package(:jar)
    end
    
    desc "A completely insecure implementation for integrated tests and the like"
    define "insecure-plugin" do
      compile.with project('plugin-api').and_dependencies
      test.with project('plugin-api').test_dependencies, 
        project('domain').and_dependencies, project('domain').test_dependencies
      package(:jar)
    end
  end

  desc "External data-providing plugins" 
  define "providers" do
    desc "The interfaces under which data providers expose data"
    define "api" do
      compile.with project('domain').and_dependencies
      package(:jar)
    end
    
    desc "Mock data providers with static data"
    define "mock" do
      compile.with project('providers:api').and_dependencies, SPRING
      test.with UNIT_TESTING
      package(:jar)
    end
  end
  
  desc "Core data access, serialization and non-substitutable business logic"
  define "core" do
    project('providers') # Have to reference this before refing project('providers:mock') in buildr 1.3.3 for some reason.  Investigate later.  RMS20090331.
    
    resources.filter.using(:ant, 
      'application-short-name'  => APPLICATION_SHORT_NAME,
      "buildInfo.versionNumber" => project.version,
      "buildInfo.username"      => ENV['USER'],
      "buildInfo.hostname"      => `hostname`.chomp,
      "buildInfo.timestamp"     => Time.now.strftime("%Y-%m-%d %H:%M:%S")
    )
    
    compile.with project('domain').and_dependencies,
      project('authentication:plugin-api').and_dependencies,
      project('authentication:local-plugin').and_dependencies, # since it's the default
      project('providers:mock').and_dependencies,
      project('database').and_dependencies,
      XML, RESTLET.framework, FREEMARKER, CSV,
      QUARTZ, 
      SPRING_WEB # tmp for mail

    test.with UNIT_TESTING, project('domain').test.compile.target, 
      project('authentication:plugin-api').test_dependencies,
      project('database').test_dependencies

    package(:jar)
    package(:sources)
    
    check do
      acSetup = File.read(_('target/resources/applicationContext-setup.xml'))
      
      acSetup.should include(`hostname`.chomp)
      acSetup.should include(project.version)
    end
  end # core
  
  desc "Web interfaces, including the GUI and the RESTful API"
  define "web" do
    compile.with LOGBACK, 
      project('core').and_dependencies,
      %w(cas websso local insecure).collect { |p| project("psc:authentication:#{p}-plugin").and_dependencies },
      SPRING_WEB, RESTLET, WEB, CAGRID

    test.with project('test-infrastructure'), 
      project('test-infrastructure').compile.dependencies,
      project('test-infrastructure').test.compile.dependencies

    package(:war, :file => _('target/psc.war')).tap do |war|
      war.libs -= artifacts(CONTAINER_PROVIDED)
      war.libs -= war.libs.select { |artifact| artifact.respond_to?(:classifier) && artifact.classifier == 'sources' }
    end
    package(:sources)
    
    iml.add_component("FacetManager") do |component|
      component.facet :type => 'web', :name => 'Web' do |facet|
        facet.configuration do |conf|
          conf.descriptors do |desc|
            desc.deploymentDescriptor :name => 'web.xml', 
              :url => "file://$MODULE_DIR$/src/main/webapp/WEB-INF/web.xml", 
              :optional => "false", :version => "2.4"
          end
          conf.webroots do |webroots|
            webroots.root :url => "file://$MODULE_DIR$/src/main/webapp", :relative => "/"
          end
        end
      end
    end
    
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
    
    task :local_jetty do
      ENV['test'] = 'no'
      set_db_name 'datasource'
      
      task(:jetty_deploy_exploded).invoke
      
      msg = "PSC deployed at #{jetty.url}/psc.  Press ^C to stop."
      info "=" * msg.size
      info msg
      info "=" * msg.size

      # Keep the script running until interrupted
      while(true)
        sleep(1)
      end
    end
    
    directory _('tmp/logs')
    
    task :jetty_deploy_exploded => ['psc:web:explode', _('tmp/logs')] do
      logconfig = _('src/main/webapp/WEB-INF/classes/logback.xml')
      rm _('src/main/webapp/WEB-INF/classes/logback.xml')
      filter(_('src/main/java')).
        using(:maven, 'catalina.home' => _('tmp').to_s).
        include(File.basename(logconfig)).
        into(File.dirname(logconfig)).
        run
      Java.java.lang.System.setProperty("logback.configurationFile", logconfig)

      jetty.deploy "#{jetty.url}/psc", _('src/main/webapp').to_s
    end
    
    # exclude exploded files from IDEA
    iml.excluded_directories << _('src/main/webapp/WEB-INF/lib') << _('src/main/webapp/WEB-INF/classes')
    
    # clean exploded files, too
    clean { 
      rm_rf _('src/main/webapp/WEB-INF/lib')
      rm_rf _('src/main/webapp/WEB-INF/classes') 
    }
  end
  
  desc "Common test code for both the module unit tests and the integrated tests"
  define "test-infrastructure", :base_dir => _('test/infrastructure') do
    compile.with UNIT_TESTING, INTEGRATED_TESTING, SPRING_WEB,
      project('core'), project('core').compile.dependencies
    test.with project('core').test.compile.target, 
      project('core').test.compile.dependencies
    package(:jar)
    package(:sources)
  end
  
  desc "Integrated tests for the RESTful API"
  define "restful-api-test", :base_dir => _('test/restful-api') do
    # Only set_db after everything is built
    task :set_db => project('psc:web').task(:explode) do
      set_db_name(ENV['INTEGRATION_DB'] || 'rest-test')
      test.options[:properties]['psc.config.datasource'] = db_name
    end
    
    compile.with(project('web'), project('web').compile.dependencies)
    test.using(:integration, :rspec).
      with(
        project('test-infrastructure'), 
        project('test-infrastructure').compile.dependencies, 
        project('test-infrastructure').test.compile.dependencies
      ).using(
        :gems => { 'rest-open-uri' => '1.0.0', 'builder' => '2.1.2', 'json_pure' => '1.1.3' },
        :requires => %w(spec http static_data).collect { |help| _("src/spec/ruby/#{help}_helper.rb") }, # + [_('src/spec/ruby/buildr-252-patches.rb')],
        :properties => { 
          'applicationContext.path' => File.join(test.resources.target.to_s, "applicationContext.xml"),
        }
      )
    test.resources.filter.using(:ant, :'resources.target' => test.resources.target.to_s)

    integration.setup {
      task(:set_db).invoke 
      task('psc:web:jetty_deploy_exploded').invoke
    }
    
    desc "One-time setup for the RESTful API integrated tests"
    task :setup => [:set_db, :'test:compile', project('psc:database').task('migrate')] do
      Java::Commands.java(
        'edu.northwestern.bioinformatics.studycalendar.test.restfulapi.OneTimeSetup', project('psc')._, 
        :classpath => test.compile.dependencies,
        :properties => { "psc.config.datasource" => db_name })
    end
  end
  
  # This is just a direct port from ant -- might be possible to do something better with buildr
  desc "Build the binary distribution package"
  task :dist do |task|
    class << task; attr_accessor :filename; end
    ENV['test'] = 'no'
    task('psc:web:package').invoke

    dist_dir = "target/dist/bin"
    mkdir_p _("#{dist_dir}/conf-samples")
    cp _("db/datasource.properties.example"), _("#{dist_dir}/conf-samples/datasource.properties")
    cp project('web').packages.select { |p| p.type == :war }.to_s, _("#{dist_dir}/psc.war")
    puts `svn export https://svn.bioinformatics.northwestern.edu/studycalendar/documents/PSC_Install_Guide.doc #{_("#{dist_dir}/psc_install.doc")}`

    task.filename = _("target/dist/psc-#{VERSION_NUMBER}-bin.zip")
    zip(task.filename).path("psc-#{VERSION_NUMBER}").include("#{dist_dir}/*").root.invoke
  end
end

###### Shared configuration

projects.each do |p|
  if File.exist?(p._("src/main/java"))
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
task :migrate => 'psc:database:migrate'

desc "Manually create the HSQLDB instance for unit testing"
task :create_hsqldb => 'psc:database:create_hsqldb'

desc "Run PSC from #{jetty.url}/psc"
task :server => 'psc:web:local_jetty'

###### Continuous integration

namespace :ci do
  desc "Continuous unit test build"
  task :unit => [:clean, :'psc:database:clean_hsqldb'] do
    task('psc:database:migrate').invoke unless hsqldb?
    task(:test).invoke
    if emma?
      [:'emma:html', :'emma:xml'].each { |t| task(t).invoke }
    end
  end
  
  task :nightly => [:unit, 'psc:dist', :artifacts_dir] do
    now = Time.now.strftime "%Y%m%d-%H%M%S"
    cp task('psc:dist').filename, "#{task('ci:artifacts_dir').dir}/psc-#{VERSION_NUMBER}-#{now}.zip"
  end
  
  directory project('psc')._('target/artifacts')
  task :artifacts_dir => project('psc')._('target/artifacts') do |task|
    class << task; attr_accessor :dir; end
    task.dir = task.prerequisites.first.to_s
  end
end