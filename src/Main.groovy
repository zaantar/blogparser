package blogparser

class Main {

    static main(args) {

        /* intro */        
        println "hello world!"
        println "this is groovy-based blogparser"
        println "copyleft 2013 zaantar (http://zaantar.eu)"
        println ""
        
        /* retrieve input parameters */
        def readln = javax.swing.JOptionPane.&showInputDialog
        def blogName, outputPath
        if(args.size() == 2) {
            blogName = args[0]
            outputPath = args[1]
            println "CLI input: blog name is '$blogName', output path is '$outputPath'"
        } else {
            println "parameters not specified clearly, please provide."
            blogName = readln "blog name"
            outputPath = readln "output path"
        }
        
        /* initialize logger */
        def logger = new ConsoleLogger();
        
        /* parse */
        logger.log "PARSING"
        def parser = new BlogCzParser(logger: logger)
        def blogContainer = parser.parse(blogName)
		
		// output
		logger.log "WRITING"
		logger.down()
		
		// initialize writer
		def outputWxrFilename = outputPath + "/" + blogName + ".wxr"
		logger.log "writing wxr file to '$outputWxrFilename"
		FileWriter writer = new FileWriter(outputWxrFilename)
		
		// build wxr
		def builder = new WxrBuilder(logger)
		builder.build(blogContainer, writer)
		writer.close()
		
		logger.up()
        
		logger.log "FINISHED"
    }

}