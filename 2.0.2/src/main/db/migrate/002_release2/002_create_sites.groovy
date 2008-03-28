class CreateSites extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        createTable('sites') { t ->
            t.addColumn('name', 'string', nullable:false)
            t.addColumn('version', 'integer', nullable:false)
        }
    }

    void down() {
        dropTable('sites')
    }
}