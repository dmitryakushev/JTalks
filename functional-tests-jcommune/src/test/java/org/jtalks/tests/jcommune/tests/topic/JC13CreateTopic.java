package org.jtalks.tests.jcommune.tests.topic;

import org.jtalks.tests.jcommune.pages.TopicPage;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import utils.StringHelp;

import static org.jtalks.tests.jcommune.common.JCommuneSeleniumTest.*;
import static org.jtalks.tests.jcommune.Assert.Exsistence.*;
import static org.jtalks.tests.jcommune.pages.TopicPage.*;

/**
 * @autor masyan
 * @autor erik
 */
public class JC13CreateTopic {

	String subject = StringHelp.getRandomString(20);
	String message = StringHelp.getRandomString(20);
	TopicPage topicPage;

	@BeforeMethod
	@Parameters({"app-url", "uUsername", "uPassword"})
	public void setupCase(String appUrl, String username, String password) {
		driver.get(appUrl);
		signIn(username, password);
		clickOnRandomBranch();
		topicPage = new TopicPage(driver);
	}

	@AfterMethod
	@Parameters({"app-url"})
	public void destroy(String appUrl) {
		logOut(appUrl);
	}

	@Test
	public void createTopicTest() {
		//first step
		topicPage.getNewButton().click();

		Assert.assertTrue(topicPage.getSubjectField().isDisplayed());
		Assert.assertTrue(topicPage.getMessageField().isDisplayed());

		//second step
		topicPage.getSubjectField().sendKeys(subject);
		topicPage.getMessageField().sendKeys(message);
		topicPage.getPostButton().click();
		Assert.assertEquals(topicPage.getTopicMessage().getText(), subject);

		assertContainsInString(topicPage.getTopicMessage().getText(), message);
	}

}