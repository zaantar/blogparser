package blogparser

import java.text.SimpleDateFormat;
import java.util.Locale;

import javax.xml.bind.annotation.XmlMimeType;

import groovy.xml.MarkupBuilder

class WxrBuilder {
	
	// Logging
	private logger
	private log(msg) {
		logger.log msg
	}
	
	private logdown() { logger.down() }
	private logup() { logger.up() }
	
	// builder
	private xml
	
	WxrBuilder(logger) {
		this.logger = logger
	}
	
	// blog content
	private b
	
	// date formatters
	// taken from old java blogparser
	private static SimpleDateFormat Rfc822DateFormat = new SimpleDateFormat("EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss' 'Z", Locale.US);
	private static SimpleDateFormat PostDateFormat = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss", Locale.US);
	
	public build(blogContent, writer) {
		log "starting export to WXR file"
		logdown()
		
		xml = new MarkupBuilder(writer)
		xml.rss(
				version: "2.0", 
				"xmlns:excerpt":"http://wordpress.org/export/1.1/excerpt/",
				"xmlns:content":"http://purl.org/rss/1.0/modules/content/",
				"xmlns:wfw":"http://wellformedweb.org/CommentAPI/",
				"xmlns:dc":"http://purl.org/dc/elements/1.1/",
				"xmlns:wp":"http://wordpress.org/export/1.1/" )
			{
				buildChannel(xml, blogContent)
			}
		log "export finished"
		logup()
	}
	
	
	private buildChannel(xml, b) {
		log "building channel"
		logdown()
		
		xml.channel {
			title(b.title)
			description(b.tagline)
			pubDate(Rfc822DateFormat.format(new Date())) // now
			link(b.url)
			language("cs")
			"wp:wxr_version"("1.1")
			"wp:base_site_url"(b.hostingUrl)
			"wp:base_blog_url"(b.url)
			
			buildAuthors(xml, b)
			// TODO build categories, tags if needed
			buildItems(xml, b)
		}
		log "channel was built"
		logup()
	}
	
	
	private buildAuthors(xml, b) {
		// blog.cz allows to wildly use different author name for each post
		// we expect that there is only one user (whose username is identical to blog's slug)
		xml."wp:author" {
			"wp:author_id"(1)
			"wp:author_login"(b.slug)
		}
	}
	
	
	private buildItems(xml, b) {
		log "building items"
		logdown()
		for(def p : b.posts) {
			log "building post '$p.slug'"
			xml.item {
				title(p.title)
				link(p.url)
				pubDate(Rfc822DateFormat.format(p.date))
				"dc:creator"(p.author)
				guid(isPermalink:"false", p.url)
				description()
				"content:encoded" {
					mkp.yieldUnescaped "<![CDATA[$p.content]]>"
				}
				"wp:post_id"(p.id)
				"wp:post_date"(PostDateFormat.format(p.date))
				"wp:comment_status"("open")
				"wp:ping_status"("open")
				"wp:post_name"(p.slug)
				"wp:status"("publish")
				"wp:post_parent"(0)
				"wp:post_type"("post")
				"wp:is_sticky"(0)
				buildItemCategories(xml, p)
				// TODO buildItemTags, wp:postmeta
				buildComments(xml, p)
			}
		}
		logup()
	}
	
	
	private buildItemCategories(xml, p) {
		// each post has up to one category on blog.cz
		if(p.category) {
			xml.category(domain:"category") {
				mkp.yieldUnescaped "<![CDATA[$p.category]]>"
			}
		}
	}
	
	
	private buildComments(xml, p) {
		for(def c: p.comments) {
			xml."wp:comment" {
				"wp:comment_id"(c.id)
				"wp:comment_author"(c.author)
				"wp:comment_author_email"(c.email)
				"wp:comment_author_url"(c.url)
				"wp:comment_date"(PostDateFormat.format(c.date))
				"wp:comment_content" {
					mkp.yieldUnescaped "<![CDATA[$c.content]]>"
				}
				"wp:comment_approved"(1)
				"wp:comment_type"("normal")
			}
		}
	}
}
