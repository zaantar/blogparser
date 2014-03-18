package blogparser

class BlogCzXpath {

	/*=== HOMEPAGE SECTION ===*/	
	static title = "/html/head/title"

	// blog tagline
	// ex: <meta name="description" content="text1 Blog.cz - Stačí otevřít a budeš v obraze." />
	// we trim the blog.cz nonsense
	static tagline = "substring-before(/html/head/meta[@name='description']/@content, ' Blog.cz')"

	/*=== ARCHIVE SECTION ===*/

	// list of links to month archive pages
	// we skip the <div> with "box" class because that may be on every page in menu, but neither
	// it's presence nor completeness is guaranteed
	static monthArchiveLinks = "//div[@id='archive' and not (@class='box')]//a/@href"

	// list of post links on month archive page
	// two formats are possible, either there are post excerpts with links in h3 titles
	// or there is a list with links to posts - but also to their categories, which we don't want to know
	static postLinksH3 = "//h3/a/@href"
	static postLinksUlLi = "//div[@id='mainInner']/ul/li/a[not(starts-with(@href, '/rubrika/'))]/@href"

	// next MAL on MA page
	static nextMAPage = "//div[@class='paginator2']/a[@title='Další stránka']/@href"

	/*=== POST SECTION ===*/

	// post title is in a h2 element
	static postTitle = "//div[@class='article']/h2"

	// date is a first text in the article div (between title and div with article content)
	// however there may be other values, in which case they are separated by "|"
	// this is a dirty hack to get an if-then-else statement with xpath 1.0.
	// see http://stackoverflow.com/questions/12977309/nested-conditional-if-else-statements-in-xpath
	static postDate = '''
		concat(
			substring(
				substring-before(//div[@class='article']/text()[1], ' |'), 
				1 div contains(//div[@class='article']/text()[1],' |') 
			), 
			substring(
				//div[@class='article']/text()[1], 
				1 div not( contains(//div[@class='article']/text()[1],' |') )
			)
		)
		'''
	
	// first link inside the article div is a link to category listing
	static postCategory = "//div[@class='article']/a[1]/text()"
	
	static postContent = "//div[@class='article']/div[@class='articleText']"
	
	
	/*=== COMMENT SECTION ===*/
	
	// div with all comments. following queries are relative to a comment node
	static commentSection = "//div[@id='komentare']/div[contains(@class,'comment')]"
	
	static commentAuthor = "div[@class='commentHeader']/strong/text()"
	
	// comment e-mail _may_ be (in malformed version) present as a mailto: link
	static commentEmail = "substring-after(div[@class='commentHeader']/a[starts-with(@href, 'mailto:')]/@href, 'mailto:')"
	
	static commentUrl = "div[@class='commentHeader']/a[@title='Web']/@href"
	
	// comment date should be allways present
	// it is only text element within commentHeader div that is surrounded by ' | '
	// the substring construct is a replacement for ends-with, which is not available in xpath 1.0
	// we need to check for string length as well, otherwise the " | " separator itself would be matched
	static commentDate = "div[@class='commentHeader']/text()[starts-with(., ' | ') and substring(., string-length() -2) = ' | ' and string-length() > 6]"
	
	static commentContent = "div[@class='commentText']/p"
	
}
