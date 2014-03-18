package blogparser

import org.w3c.tidy.Tidy;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.nio.CharBuffer;

import org.w3c.dom.*;

import java.util.Random;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;

class DirtyHTMLReader {

	// JTidy instance
	Tidy tidy

	// URL builder
	def url

	// random number generator
	def rnd

	// logger
	def logger
	private log(msg) {
		logger.log msg
	}
	
	private logdown() { logger.down() }
	private logup() { logger.up() }

	// how many attempts to download a page before exception
	def attemptCount = 5
	// how many seconds to wait to next attempt
	def pauseBetweenAttempts = 10

	def minPause = 0
	def randomPause = 1500

	// xpath instance (for executing xpath queries)
	private xp
	
	DirtyHTMLReader(url, logger) {

		tidy = new Tidy()
		tidy.setQuiet true
		tidy.setShowWarnings false
		//tidy.setShowErrors 0
		tidy.setXHTML true
		//tidy.setForceOutput true

		this.url = url
		
		this.logger = logger

		this.xp = XPathFactory.newInstance().newXPath()
		
		def rndGen = {
			randomizer -> minPause + randomizer.nextInt(randomPause)
		}
		rnd = rndGen.curry(new Random())
	}
	
	public read(relUrl) {
		def fullUrl = url relUrl
		log "reading page $fullUrl"
		logdown()
		
		def sleepTime = rnd()
		log "waiting for $sleepTime ms"
		sleep sleepTime
		
		URL urlObject = new URL(fullUrl)

		log "downloading..."

		def result
		def currentAttempt = 1
		def success = false
		while(currentAttempt < attemptCount && !success) {

			try {
				def inputStream = urlObject.openStream()
				result = tidy.parseDOM(new InputStreamReader(killBadFacebookCode(inputStream), "UTF-8"), null)
				//printDocument(result, System.out)
				success = true
			} catch(e) {
				log "catched exception '" + e.toString() + "' while downloading the page."

				log "waiting for $pauseBetweenAttempts seconds"
				sleep pauseBetweenAttempts * 1000

				currentAttempt++
				log "trying again - attempt $currentAttempt of $attemptCount"
			}
		}

		if(success) {
			log "finished"
			logup()
		} else {
			logup()
			throw new BlogparserException("couldn't download the page")
		}
		
		result
	}
		
		
	public printDocument(Document doc, OutputStream out)  {
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
	
		transformer.transform(new DOMSource(doc),
			 new StreamResult(new OutputStreamWriter(out, "UTF-8")));
	}


	private xpath(document, query, what) {
		logdown()
		log "executing xpath query: $query"
		def result = xp.evaluate(query, document, what)
		logup()
		result
	}

	
	public xpathString(document, query) {
		xpath(document, query, XPathConstants.STRING)
	}
	
	
	private transformNodeset(nodeset, transform) {
		def result = []
		for(def i = 0; i < nodeset.length; ++i) {
			result.add(transform(nodeset.item(i)))
		}
		result
	}
	
	
	public nodeToString(node) {
		StringWriter sw = new StringWriter();
		Transformer t = TransformerFactory.newInstance().newTransformer();
		t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		t.setOutputProperty(OutputKeys.METHOD, "html");
		t.transform(new DOMSource(node), new StreamResult(sw));
		return sw.toString();
	}

	
	public xpathList(document, query) {
		transformNodeset xpath(document, query, XPathConstants.NODESET), {node-> node.nodeValue}
	}
	
	
	public xpathNodeAsString(document, query) {
		nodeToString xpath(document, query, XPathConstants.NODE)
	}
	

	public xpathNodeset(document, query) {
		def nodeset = xpath(document, query, XPathConstants.NODESET)
		transformNodeset nodeset, {node-> node}
	}
	
	
	private inputStreamToString(inputStream) {
		InputStreamReader is = new InputStreamReader(inputStream);
		StringBuilder sb=new StringBuilder();
		BufferedReader br = new BufferedReader(is);
		String read = br.readLine();
		while(read != null) {
			sb.append(read);
			read =br.readLine();
		}	
		return sb.toString();
	}
	
	
	private stringToInputStream(str) {
		InputStream is = new ByteArrayInputStream(str.getBytes("UTF-8"));
		is
	}
	
	
	private killBadFacebookCode(rawHtmlInputStream) {
		def rawHtml = inputStreamToString(rawHtmlInputStream)
		def str = rawHtml.replace("<fb:like", "<span").replace("</fb:like>", "</span>")
		
		// JTidy chucks up on this. KILL IT WITH FIRE.
		// I spent way too many hours to figure this out. DAMN YOU FACEBOOK!!!
		def reallyBadCode = '''<div id="fb-root">		<script type="text/javascript">			window.fbAsyncInit = function() {				FB.init({appId: '152936751395548', status: true, cookie: true, xfbml: true});			};			$('#fb-root').append('<script src="http://connect.facebook.net/cs_CZ/all.js" async="true"></' + 'script>');		</script>	</div>'''
		str = str.replace(reallyBadCode, "")
		
		stringToInputStream(str)
	}
	
	
	
}
