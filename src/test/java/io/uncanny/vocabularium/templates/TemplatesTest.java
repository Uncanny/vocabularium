/**
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   This file is part of the Uncanny Vocabularium project:
 *     http://uncanny.io/vocabularium/
 *
 *   Uncanny Software Projects
 *     http://uncanny.io/
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Copyright (C) 2016-2017 Uncanny Software Projects.
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package io.uncanny.vocabularium.templates;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isIn;
import static org.hamcrest.Matchers.notNullValue;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.CSSParseException;
import org.w3c.css.sac.ErrorHandler;

import com.gargoylesoftware.htmlunit.IncorrectnessListener;
import com.gargoylesoftware.htmlunit.InteractivePage;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.ScriptException;
import com.gargoylesoftware.htmlunit.StringWebResponse;
import com.gargoylesoftware.htmlunit.TopLevelWindow;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.attachment.AttachmentHandler;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HTMLParser;
import com.gargoylesoftware.htmlunit.html.HTMLParserListener;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.JavaScriptErrorListener;
import com.google.common.collect.Lists;

import io.uncanny.vocabularium.model.Example;
import io.uncanny.vocabularium.model.Site;

public class TemplatesTest {

	private static final class LocalWindow extends TopLevelWindow {

		private static WebClient webClient;

		static {
			webClient = new WebClient();
			webClient.getOptions().setCssEnabled(false);
			webClient.getOptions().setJavaScriptEnabled(false);
			webClient.setIncorrectnessListener(new IncorrectnessListener() {
				@Override
				public void notify(String message, Object origin) {
					System.out.println("Incorrectness: "+message+" ["+origin+"]");
				}});
			webClient.setAttachmentHandler(new AttachmentHandler() {
				@Override
				public void handleAttachment(Page page) {
					System.out.println("Attachment: "+page);
				}});
			webClient.setCssErrorHandler(new ErrorHandler(){
				@Override
				public void warning(CSSParseException exception) throws CSSException {
					System.out.println("CSS parse warning: "+exception);
				}
				@Override
				public void error(CSSParseException exception) throws CSSException {
					System.out.println("CSS parse error: "+exception);
				}
				@Override
				public void fatalError(CSSParseException exception) throws CSSException {
					System.out.println("Fatal error: "+exception);
				}});
			webClient.setHTMLParserListener(new HTMLParserListener() {
				@Override
				public void error(String message, URL url, String html, int line, int column, String key) {
					System.out.printf("HTML parse error: %s [line: %d, column: %d]%n",message,line,column);
				}
				@Override
				public void warning(String message, URL url, String html, int line, int column, String key) {
					System.out.printf("HTML parse warning: %s [line: %d, column: %d]%n",message,line,column);
				}});
			webClient.setJavaScriptErrorListener(new JavaScriptErrorListener() {
				@Override
				public void scriptException(InteractivePage page, ScriptException scriptException) {
					System.out.println("Script exception: "+page+", "+scriptException);
				}
				@Override
				public void timeoutError(InteractivePage page, long allowedTime, long executionTime) {
					System.out.println("Script timeout error: "+page+" ["+allowedTime+" < " +executionTime+"]");
				}
				@Override
				public void malformedScriptURL(InteractivePage page, String url,MalformedURLException malformedURLException) {
					System.out.println("Malformed script URL: "+page+" ["+url+" : " +malformedURLException.getMessage()+"]");
				}
				@Override
				public void loadScriptError(InteractivePage page, URL scriptUrl, Exception exception) {
					System.out.println("Load script error: "+page+" ["+scriptUrl+" : "+exception.getMessage()+"]");
				}});

		}
		private static final long serialVersionUID = 6422051966953718217L;

		private LocalWindow() {
			super("Test window",webClient);
		}
	}

	private static Site SITE;
	private static HtmlPage PAGE;

	@BeforeClass
	public static void setUpBefore() throws Exception {
		SITE = Example.site();
		PAGE = constructPage(Templates.catalogRepresentation(SITE));
	}

	@Test
	public void pageShouldHaveSpecifiedLanguage() {
		assertThat(PAGE.getElementsByTagName("html").get(0).getAttribute("lang"),equalTo(SITE.getMetadata().getLanguage()));
	}

	@Test
	public void pageShouldHaveSpecifiedTitle() {
		assertThat(PAGE.getTitleText(),equalTo(SITE.getTitle()));
	}

	@Test
	public void pageMetadataShouldIncludeSpecifiedKeywords() {
		final DomNodeList<HtmlElement> metas = PAGE.getHead().getElementsByTagName("meta");
		final boolean hasKeywords=!SITE.getMetadata().getKeywords().isEmpty();
		boolean keywordsVerified=false;
		for(int i=0;i<metas.getLength();i++) {
			final HtmlElement element=metas.get(i);
			final String name = element.getAttribute("name");
			if("keywords".equals(name)) {
				assertThat(keywordsVerified,equalTo(false));
				final String rawKeywords = element.getAttribute("content");
				assertThat(rawKeywords,notNullValue());
				final List<String> keywords=Lists.newArrayList(rawKeywords.split(","));
				for(int j=1;j<keywords.size();j++) {
					assertThat(keywords.get(j).trim(),isIn(SITE.getMetadata().getKeywords()));
					keywordsVerified=true;
				}
			}
		}
		assertThat(keywordsVerified,equalTo(hasKeywords));
	}

	@Test
	public void pageMetadataShouldIncludeSpecifiedAuthors() {
		final DomNodeList<HtmlElement> metas = PAGE.getHead().getElementsByTagName("meta");
		final List<String> authors=Lists.newArrayList(SITE.getMetadata().getAuthors());
		for(int i=0;i<metas.getLength();i++) {
			final HtmlElement element=metas.get(i);
			final String name = element.getAttribute("name");
			if("author".equals(name)) {
				final String attributeContent = element.getAttribute("content");
				assertThat(attributeContent.trim(),isIn(authors));
				assertThat(authors.remove(attributeContent),equalTo(true));
			}
		}
		assertThat(authors,hasSize(0));
	}

	@Test
	public void pageHeadingShouldHaveSpecifiedTitle() {
		final DomNodeList<HtmlElement> divs = PAGE.getBody().getElementsByTagName("div");
		divs.
			stream().
				filter((HtmlElement e) -> "jumbotron".equals(e.getAttribute("class"))).
				map((HtmlElement e) -> e.getElementsByTagName("h1").get(0).asText()).
				forEach((String s) -> assertThat(s,equalTo(SITE.getTitle())));
	}

	@Test
	public void pageHeadingShouldPointToSpecifiedOwner() {
		final DomNodeList<HtmlElement> divs = PAGE.getBody().getElementsByTagName("div");
		divs.
			stream().
				filter((HtmlElement e) -> "jumbotron".equals(e.getAttribute("class"))).
				map((HtmlElement e) -> e.getElementsByTagName("a").get(0)).
				forEach((HtmlElement a) -> {
					assertThat(a.getAttribute("href"),equalTo(SITE.getOwner().getUri()));
					assertThat(a.asText(),equalTo(SITE.getOwner().getName()));
				});
	}

	private static HtmlPage constructPage(final String aHtmlCode) throws IOException {
		dumpPage(aHtmlCode);
		return
			HTMLParser.
				parseHtml(
					new StringWebResponse(
						aHtmlCode,
						new URL("http://uncanny.io/test")),
					new LocalWindow());
	}

	private static void dumpPage(final String catalogRepresentation) {
		if(System.getProperty("dump")!=null) {
			String lines[] = catalogRepresentation.split("\\r?\\n");
			for(int i=1;i<lines.length+1;i++) {
				System.out.printf("[%03d] %s%n",i,lines[i-1]);
			}
		}
	}


}
