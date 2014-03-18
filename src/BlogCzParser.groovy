package blogparser

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

class BlogCzParser extends Parser {

	// BlogContainer
	private b
	
	// Closure that will return full url when given a relative to blog root 
	private url

	// Dirty HTML reader 
	private reader

	// Logging
	private logger
	private log(msg) {
		logger.log msg
	}
	
	private logdown() { logger.down() }
	private logup() { logger.up() }


	// misc
	private blogIdentifier
	
	@Override
	public BlogContainer parse(blog_identifier) {
		
		log "BlogCzParser greets you"
		log "parsing blog $blog_identifier"
		logdown()
		
		b = new BlogContainer()
		blogIdentifier = blog_identifier

		// determine blog URL and prepare URL builder
		def baseUrl = "http://$blog_identifier"
		
		def getCustomUrl = {blogIdentifier, path = "" ->
			if(path.size() >= 7 && path.substring(0, 6) == "http://") {
				path
			} else {
				if(path.size() > 0 && path[0] != '/') {
					path = "/" + path
				}
				"http://$blogIdentifier" + ".blog.cz$path" 
			}
		}
		url = getCustomUrl.curry(blog_identifier)
		
		log "base url is " + url()

		// prepare dirty HTML reader
		reader = new DirtyHTMLReader(url, logger)

		// parsing
		retrieveMetadata()
		retrieveAllPosts()
		retrieveCategories()
		
		logup()
		log "BlogCzParser operation finished"
		
		return b
	}

	
	/** Read and store blog's metadata such as title, tagline, author. */
	private retrieveMetadata() {
		log "retrieving blog metadata"
		logdown()

		b.slug = blogIdentifier
		b.url = url()
		b.hostingUrl = "http://blog.cz"
		
		def homepage = reader.read ""

		def title = reader.xpathString homepage, BlogCzXpath.title
		b.title = title
		log "title is '$title'"

		b.tagline = reader.xpathString homepage, BlogCzXpath.tagline
		log "tagline is $b.tagline"

		b.author = blogIdentifier
		logup()
	}


	private retrieveAllPosts() {
		log "retrieving posts"
		logdown()

		def postLinks = retrieveAllPostLinks()

		log "start parsing posts"

		def posts = []
		def postId = 0
		for(def postLink : postLinks) {
			posts.add parsePost(postLink, ++postId)
		}
		
		log "got $posts.size in total"
		b.posts = posts
		
		logup()
	}


	private retrieveAllPostLinks() {
		log "retrieving post links"
		logdown()

		// get month archive links (MAL)
		def monthArchiveLinks = retrieveMALs()

		// process MALs to get post links
		def postLinks = []
		// MALs may be paginated, so we need possibility to add links to this collection
		def enqueueLink = {collection, link -> collection.add(link)}.curry(monthArchiveLinks)
		while(monthArchiveLinks) {
			postLinks += retrievePLFromMAL(monthArchiveLinks.pop(), enqueueLink)
		}

		log "got $postLinks.size post links in total"
		logup()
		postLinks
	}


	/** Read main archive page and return list of links to pages with month's posts */
	private retrieveMALs() {
		log "retrieving month archive links"
		logdown()

		def archivePage = reader.read "archiv"

		def result = reader.xpathList archivePage, BlogCzXpath.monthArchiveLinks
		log "got $result.size links"
	
		logup()
		result
	}


	private malFormat = null

	private retrievePLFromMAL(link, enqueueLink) {
		log "retrieving post links from archive $link"
		logdown()

		def malPage = reader.read link

		// determine MAL format if it isn't known yet		
		if(!malFormat) {
			malFormat = getMALFormat(malPage)
		}

		// try "next page" link in MA
		def nextMAL = reader.xpathString malPage, BlogCzXpath.nextMAPage
		if(nextMAL) {
			log "found next MAL: $nextMAL, enqueuing"
			enqueueLink nextMAL
		}

		// get post links
		def postLinks = reader.xpathList malPage, malFormat
		log "got $postLinks.size post links."
		
		logup()
		postLinks
	}


	private getMALFormat(malPage) {
		log "detecting MAL format"
		logdown()

		log "trying H3"
		if(reader.xpathList(malPage, BlogCzXpath.postLinksH3).size > 0) {
			log "H3 MAL format match"
			logup()
			return BlogCzXpath.postLinksH3
		}

		log "trying UL/LI"
		if(reader.xpathList(malPage, BlogCzXpath.postLinksUlLi).size > 0) {
			log "UL/LI MAL format match"
			logup()
			return BlogCzXpath.postLinksUlLi
		}

		logup()
		throw new BlogparserException("Cannot determine MAL format. No post lins retrieved whatsoever.")
	}


	private parsePost(postLink, postId) {
		log "parsing post from $postLink"
		logdown()

		def page = reader.read postLink

		def post = new Expando()

		// trivially figure some post metadata
		post.id = postId
		post.author = blogIdentifier
		post.url = url postLink
		post.guid = post.url
		post.slug = post.url.substring(1)

		// read from page
		post.title = reader.xpathString(page, BlogCzXpath.postTitle)
		log "post title is $post.title"

		post.date = parseDate reader.xpathString(page, BlogCzXpath.postDate)
		log "post date is $post.date"
		
		post.category = reader.xpathString(page, BlogCzXpath.postCategory)
		log "post category is '$post.category'"
		
		post.content = parsePostContent reader.xpathNodeAsString(page, BlogCzXpath.postContent)
		log "post content is '$post.content'"
		
		post.comments = parseComments page
				
		logup()
		post
	}


	private parseDate(dateInput) {
	
		// following /MESSY/ code is taken from old java blogparser
		String dateString = dateInput.replace(" ledna ", "1.").replace(" února ", "2.").replace(" března ", "3.").replace(" dubna ", "4.").replace(" května ", "5.").replace(" června ", "6.").replace(" července ", "7.").replace(" srpna ", "8.").replace(" září ", "9.").replace(" října ", "10.").replace(" listopadu ", "11.").replace(" prosince ", "12.");
		
		dateString = dateString.replace(" | ", "")
		
		DateFormat dr = new SimpleDateFormat("d.M.y");
		
		Calendar yesterday = Calendar.getInstance();
		yesterday.add(Calendar.DATE, -1);
		dateString = dateString.replace("Dnes", dr.format(new Date())).replace("Včera", dr.format(yesterday.getTime()));
		
		int dow = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
		def prev_dow = new Calendar[7];
		def k = 1
		for(def i=0; i<7; ++i) {
			prev_dow[i] = Calendar.getInstance();
			prev_dow[i].add(Calendar.DAY_OF_WEEK, -dow+k);
			++k
		}
		dateString = dateString.replace("Pondělí", dr.format(prev_dow[Calendar.MONDAY-1].getTime()));
		dateString = dateString.replace("Úterý", dr.format(prev_dow[Calendar.TUESDAY-1].getTime()));
		dateString = dateString.replace("Středa", dr.format(prev_dow[Calendar.THURSDAY-1].getTime()));
		dateString = dateString.replace("Čtvrtek", dr.format(prev_dow[Calendar.WEDNESDAY-1].getTime()));
		dateString = dateString.replace("Pátek", dr.format(prev_dow[Calendar.FRIDAY-1].getTime()));
		dateString = dateString.replace("Sobota", dr.format(prev_dow[Calendar.SATURDAY-1].getTime()));
		dateString = dateString.replace("Neděle", dr.format(prev_dow[Calendar.SUNDAY-1].getTime()));

		DateFormat formatter = new SimpleDateFormat("d.M.y 'v' HH:mm", new Locale("cs", "CZ"));
		Date date;
		try {
			date = formatter.parse(dateString);
		} catch (ParseException e) {
			log("date parsing error - using current date.");
			date = new Date();
		}
		return date;
	}
	
	
	private parsePostContent(content) {
		// we need to remove surrounding div tag:
		// '<div class="articleText">POST CONTENT</div>'
		content[25..-7]
	}
	
	
	/**
	 * Parses mangled "mailto:..." from blog.cz into valid e-mail address.
	 * @param nodeValue string from blog.cz
	 * @return valid e-mail address
	 */
	private parseEmail(String nodeValue) {
		return nodeValue.replace(" (V) ", "@").replace("%20(V)%20", "@").replace("mailto:", "");
	}
	
	private lastCommentId = 0
	
	private parseComments(page) {
		log "parsing comments"
		logdown()
		
		def commentNodes = reader.xpathNodeset(page, BlogCzXpath.commentSection)
		log "got $commentNodes.size comments"
		
		def result = []
		for(def commentNode : commentNodes) {
			def comment = new Expando()
			
			comment.id = ++lastCommentId
			comment.author = reader.xpathString(commentNode, BlogCzXpath.commentAuthor)
			comment.email = parseEmail reader.xpathString(commentNode, BlogCzXpath.commentEmail)
			comment.url = reader.xpathString(commentNode, BlogCzXpath.commentUrl)
			comment.date = parseDate reader.xpathString(commentNode, BlogCzXpath.commentDate)
			comment.content = reader.xpathNodeAsString(commentNode, BlogCzXpath.commentContent)
			
			log "comment date='$comment.date', author='$comment.author', e-mail='$comment.email', url='$comment.url', content='$comment.content'"
			result.add(comment)
		}		
		
		logup()
		result
	}
	
	
	private retrieveCategories() {
		// wordpress can actually extract categories from posts
		// maybe TODO later
		log "retrieving categories"
		logdown()
		
		log "this feature is currently not implemented. let's hope wordpress can handle it correctly."
		
		b.categories = []
		
		logup()
	}
}
