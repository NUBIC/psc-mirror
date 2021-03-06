# Provides extensions to Buildr's Artifact task to use locally-stored,
# bnd-wrapped versions of some libraries.
#
# The libs are stored in osgi/bundled-lib.

# Ensure that bnd.rake is already loaded.
# This may not be necessary with buildr-1.3.5
load File.expand_path(File.dirname(__FILE__) + "/bnd.rake")

def psc_osgi_artifact(spec, bnd_props={}, dst_spec_overrides={}, &src_mod)
  src_spec = Artifact.to_hash(spec)
  # wrapped id per SpringSource repo model
  dst_spec = src_spec.
    merge(:id => "edu.northwestern.bioinformatics.osgi.#{src_spec[:id]}").
    merge(dst_spec_overrides)
  unless task = Artifact.lookup(dst_spec)
    src = artifact(src_spec)
    if block_given?
      yield src
    end
    task = Osgi::BundledArtifact.define_task(
      Osgi::BundledArtifact.bundled_file(dst_spec))
    task.send :apply_spec, dst_spec
    task.init(src, bnd_props)
    task.enhance [src]
    Rake::Task['rake:artifacts'].enhance [task]
    Artifact.register(task)
  end
  task
end

module Osgi
  class BundledArtifact < Buildr::Artifact
    attr_reader :src_artifact

    def init(src_artifact, bnd_props)
      @src_artifact = src_artifact
      @bnd_props = ArtifactBndProperties.new(self).merge!(bnd_props)
      self.setup_bnd_task
    end

    def bundled_file
      self.class.bundled_file(self.to_spec_hash)
    end

    def self.bundled_file(spec_hash)
      File.join(
        basedir, 'osgi', 'bundled-lib',
        spec_hash[:group].split('.'),
        spec_hash[:id],
        spec_hash[:version],
        Artifact.hash_to_file_name(spec_hash)
        )
    end

    def bnd_file
      @bnd_file_task ||= Rake::FileTask.define_task("#{self.class.basedir}/osgi/bnd-tmp/#{Artifact.hash_to_file_name(src_artifact.to_spec_hash.merge(:type => 'bnd'))}") do |task|
        trace "Generating bnd instructions in #{task}"
        mkdir_p File.dirname(task.name)
        File.open(task.name, 'w') do |f|
          @bnd_props.write f
        end
      end
    end

    protected

    def self.basedir
      File.expand_path("..", File.dirname(__FILE__))
    end

    def setup_bnd_task
      # If the bundled file is already present, don't rebundle it
      # even if the src artifact is not on the system.
      unless File.exist?(bundled_file)
        # this deliberately does not depend on bnd_file.  This is so that
        # the bnd files don't have to be kept alongside the jars to
        # prevent them from being constantly rebuilt.
        self.enhance([src_artifact]) do |task|
          bnd_file.invoke

          mkdir_p File.dirname(task.name)
          Buildr::ant("bnd") do |ant|
            bndargs = {
              :jars => @src_artifact.to_s,
              :output => task.name,
              :definitions => File.dirname(bnd_file.name)
            }
            trace "Invoking bndwrap with #{bndargs.inspect}"

            ant.taskdef :resource => 'aQute/bnd/ant/taskdef.properties'
            info "Wrapping #{File.basename src_artifact.to_s} into #{File.basename task.name}"
            ant.bndwrap bndargs
          end

          unless Buildr.application.options.trace
            rm_rf File.dirname(bnd_file.name)
          else
            trace "Leaving behind #{bnd_file} for inspection"
          end
        end
      end
    end

    class ArtifactBndProperties
      include Bnd::BndProperties

      attr_reader :artifact

      def initialize(artifact)
        @artifact = artifact
      end

      def default_version
        artifact.version
      end

      def default_symbolic_name
        [artifact.group, artifact.id].join('.')
      end

      def default_export_packages
        ["*;version=#{version}"]
      end

      def default_import_packages
        ["*;resolution:=optional"]
      end
    end
  end
end
