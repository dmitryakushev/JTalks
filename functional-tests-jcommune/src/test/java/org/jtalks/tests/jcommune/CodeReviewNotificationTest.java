package org.jtalks.tests.jcommune;

import org.jtalks.tests.jcommune.webdriver.action.Branches;
import org.jtalks.tests.jcommune.webdriver.action.Notifications;
import org.jtalks.tests.jcommune.webdriver.action.Topics;
import org.jtalks.tests.jcommune.webdriver.action.Users;
import org.jtalks.tests.jcommune.webdriver.entity.branch.Branch;
import org.jtalks.tests.jcommune.webdriver.entity.topic.CodeReview;
import org.jtalks.tests.jcommune.webdriver.entity.topic.CodeReviewComment;
import org.jtalks.tests.jcommune.webdriver.entity.user.User;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.jtalks.tests.jcommune.webdriver.JCommuneSeleniumConfig.driver;
import static org.jtalks.tests.jcommune.webdriver.page.Pages.mainPage;

/**
 * Created by dyakushev on 2/12/2015.
 */
public class CodeReviewNotificationTest {

    public static final String BRANCH_NAME = "Notification tests";
    public static final String BRANCH_NAME_TO_MOVE_TOPIC_IN = "Notification tests branch 2";

    @BeforeMethod(alwaysRun = true)
    @Parameters({"appUrl"})
    public void setupCase(String appUrl) {
        driver.get(appUrl);
        mainPage.logOutIfLoggedIn(driver);
    }


    @Test
    public void autosubscribe_codReviewStarterAutomaticallySubscribes()
            throws Exception{
        User codReviewStarter = Users.signUpAndSignIn();
        codReviewStarter.setAutoSubscribe(true);
        CodeReview codeReview = Topics.createCodeReview(new CodeReview().withBranch(BRANCH_NAME));
        Branches.unsubscribeSafe(codeReview.getBranch()); // to avoid future branch notifications

        Topics.assertIsSubscribed(codeReview, codReviewStarter);
    }

    @Test
    public void  autosubscribe_codReviewCommentatorAutomaticallySubscribes()
            throws Exception{
        User codReviewStarter = Users.signUpAndSignIn();
        CodeReview codeReview = Topics.createCodeReview(new CodeReview().withBranch(BRANCH_NAME));
        Users.logout();

        User codReviewCommentator = Users.signUpAndSignIn();
        codReviewCommentator.setAutoSubscribe(true);
        Branches.openBranch(codeReview.getBranch().getTitle());
        Topics.openTopicInCurrentBranch(100, codeReview.getTitle());
        CodeReviewComment codeReviewComment = new CodeReviewComment().withContent(randomAlphanumeric(100));
        Topics.leaveCodeReviewComment(codeReviewComment);
        Branches.unsubscribeSafe(codeReview.getBranch());
        Users.logout();

        Topics.assertIsSubscribed(codeReview, codReviewCommentator);
    }

    @Test
    public void createCodeReview_UserSubscribedToBranchReceivesBranchNotificationOnly()
            throws  Exception{
        CodeReview codeReview = new CodeReview().withBranch(BRANCH_NAME);
        User branchSubscriber = Users.signUpAndSignIn();
        Branches.subscribe(codeReview.getBranch());
        Users.logout();

        User codReviewStarter = Users.signUpAndSignIn();
        Topics.createCodeReview(codeReview);
        Branches.unsubscribeSafe(codeReview.getBranch());
        Users.logout();

        Users.signIn(branchSubscriber);
        Branches.unsubscribeSafe(codeReview.getBranch());

        Notifications.assertNotificationOnTopicNotSentBranchSent(codeReview, branchSubscriber);
    }

    @Test
    public void createCodeReview_UserSubscribedToBranchReceiveNoBranchNoTopicNotificationIfCreatedCodeReviewHimself()
            throws  Exception{
        CodeReview codeReview = new CodeReview().withBranch(BRANCH_NAME);
        User branchSubscriber = Users.signUpAndSignIn();
        Branches.subscribe(codeReview.getBranch());
        CodeReview codeReview = Topics.createCodeReview(codeReview);
        Branches.unsubscribeSafe(codeReview.getBranch());

        Notifications.assertNotificationOnTopicNotSentBranchNotSent(codeReview, branchSubscriber);
    }

    @Test
    public void addCommentToCodeReview_UserSubscribedToCodeReviewOnlyReceivesCodeReviewNotificationOnly()
        throws Exception {
        User codReviewStarter = Users.signUpAndSignIn();
        CodeReview codeReview = Topics.createCodeReview(new CodeReview().withBranch(BRANCH_NAME));
        Topics.subscribe(codeReview);
        Branches.unsubscribe(codeReview.getBranch());
        // Method is used to make sure that user is not subscribed to the branch.
        Users.logout();

        User topicCommentator = Users.signUpAndSignIn();
        Branches.openBranch(codeReview.getBranch().getTitle());
        Topics.openTopicInCurrentBranch(100, codeReview.getTitle());
        CodeReviewComment codeReviewComment = new CodeReviewComment().withContent(randomAlphanumeric(100));
        Topics.leaveCodeReviewComment(codeReviewComment);
        Branches.unsubscribeSafe(codeReview.getBranch());
        // Method is used to make sure that user is not subscribed to the branch.

        Notifications.assertNotificationOnTopicSentBranchNotSent(codeReview, codReviewStarter);
    }

    @Test
    public void addCommentToCodeReview_UserSubscribedToBranchOnlyDoNotReceiveNotifications()
        throws Exception{
        User codReviewStarter = Users.signUpAndSignIn();
        CodeReview codeReview = Topics.createCodeReview(new CodeReview().withBranch(BRANCH_NAME));
        Topics.unsubscribe(codeReview);
        Branches.subscribe(codeReview.getBranch());
        Users.logout();

        User topicCommentator = Users.signUpAndSignIn();
        Branches.openBranch(codeReview.getBranch().getTitle());
        Topics.openTopicInCurrentBranch(100, codeReview.getTitle());
        CodeReviewComment codeReviewComment = new CodeReviewComment().withContent(randomAlphanumeric(100));
        Topics.leaveCodeReviewComment(codeReviewComment);
        Branches.unsubscribeSafe(codeReview.getBranch());
        Users.logout();

        Users.signIn(codReviewStarter);
        Branches.unsubscribeSafe(codeReview.getBranch());

        Notifications.assertNotificationOnTopicNotSentBranchNotSent(codeReview, codReviewStarter);

    }

    @Test
    public void addCommentToCodeReview_UserSubscribedBothToBranchAndCodReviewReceivesCodeReviewNotificationOnly()
        throws Exception{
        User codReviewStarter = Users.signUpAndSignIn();
        CodeReview codeReview = Topics.createCodeReview(new CodeReview().withBranch(BRANCH_NAME));
        Topics.subscribe(codeReview);
        Branches.subscribe(codeReview.getBranch());
        Users.logout();

        User topicCommentator = Users.signUpAndSignIn();
        Branches.openBranch(codeReview.getBranch().getTitle());
        Topics.openTopicInCurrentBranch(100, codeReview.getTitle());
        CodeReviewComment codeReviewComment = new CodeReviewComment().withContent(randomAlphanumeric(100));
        Topics.leaveCodeReviewComment(codeReviewComment);
        Branches.unsubscribeSafe(codeReview.getBranch());
        Users.logout();

        Users.signIn(codReviewStarter);
        Branches.unsubscribeSafe(codeReview.getBranch());

        Notifications.assertNotificationOnTopicSentBranchNotSent(codeReview, codReviewStarter);
    }

    @Test
    public void addCommentToCodeReview_UserSubscribedToCodeReviewDoNotReceiveNotificationsAboutHisOwnComments()
        throws Exception{
        User codeReviewStarter = Users.signUpAndSignIn();
        CodeReview codeReview = Topics.createCodeReview(new CodeReview().withBranch(BRANCH_NAME));
        Topics.subscribe(codeReview);
        Branches.unsubscribe(codeReview.getBranch());
        CodeReviewComment codeReviewComment = new CodeReviewComment().withContent(randomAlphanumeric(100));
        Topics.leaveCodeReviewComment(codeReviewComment);

        Notifications.assertNotificationOnTopicNotSentBranchNotSent(codeReview, codeReviewStarter);
    }

    @Test
    public void addCommentToCodeReview_UnsubscribedUserDoNotReceiveNotifications()
        throws Exception{
        User codReviewStarter = Users.signUpAndSignIn();
        CodeReview codeReview = Topics.createCodeReview(new CodeReview().withBranch(BRANCH_NAME));
        Topics.unsubscribe(codeReview);
        Branches.unsubscribe(codeReview.getBranch());
        Users.logout();

        User topicCommentator = Users.signUpAndSignIn();
        Branches.openBranch(codeReview.getBranch().getTitle());
        Topics.openTopicInCurrentBranch(100, codeReview.getTitle());
        CodeReviewComment codeReviewComment = new CodeReviewComment().withContent(randomAlphanumeric(100));
        Topics.leaveCodeReviewComment(codeReviewComment);
        Branches.unsubscribeSafe(codeReview.getBranch());

        Notifications.assertNotificationOnTopicNotSentBranchNotSent(codeReview, codeReviewStarter);
    }

    @Test
    public void deleteCodeReview_UserSubscribedToCodeReviewOnlyReceivesCodeReviewNotificationOnly()
            throws Exception {
        User codReviewStarter = Users.signUpAndSignIn();
        CodeReview codeReview = Topics.createCodeReview(new CodeReview().withBranch(BRANCH_NAME));
        Topics.subscribe(codeReview);
        Branches.unsubscribe(codeReview.getBranch());
        Users.logout();

        User codReviewDeleter = Users.signUpAndSignIn();
        Branches.openBranch(codeReview.getBranch().getTitle());
        Topics.openTopicInCurrentBranch(100, codeReview.getTitle());
        Topics.deleteTopic(codeReview);

        Notifications.assertNotificationOnTopicSentBranchNotSent(codeReview, codReviewStarter);
    }

    @Test
    public void deleteCodeReview_UserSubscribedToBranchOnlyReceivesBranchNotificationOnly()
            throws Exception{
        User codReviewStarter = Users.signUpAndSignIn();
        CodeReview codeReview = Topics.createCodeReview(new CodeReview().withBranch(BRANCH_NAME));
        Topics.unsubscribe(codeReview);
        Branches.subscribe(codeReview.getBranch());
        Users.logout();

        User codReviewDeleter = Users.signUpAndSignIn();
        Branches.openBranch(codeReview.getBranch().getTitle());
        Topics.openTopicInCurrentBranch(100, codeReview.getTitle());
        Topics.deleteTopic(codeReview);
        Branches.unsubscribeSafe(codeReview.getBranch());
        Users.logout();

        Users.signIn(codReviewStarter);
        Branches.unsubscribeSafe(codeReview.getBranch());

        Notifications.assertNotificationOnTopicNotSentBranchSent(codeReview, codReviewStarter);
    }

    @Test
    public void deleteCodeReview_UserSubscribedBothToBranchAndCodReviewReceivesCodeReviewNotificationOnly()
            throws Exception{
        User codReviewStarter = Users.signUpAndSignIn();
        CodeReview codeReview = Topics.createCodeReview(new CodeReview().withBranch(BRANCH_NAME));
        Topics.subscribe(codeReview);
        Branches.subscribe(codeReview.getBranch());
        Users.logout();

        User codReviewDeleter = Users.signUpAndSignIn();
        Branches.openBranch(codeReview.getBranch().getTitle());
        Topics.openTopicInCurrentBranch(100, codeReview.getTitle());
        Topics.deleteTopic(codeReview);
        Branches.unsubscribeSafe(codeReview.getBranch());

        Users.signIn(codReviewStarter);
        Branches.unsubscribeSafe(codeReview.getBranch());

        Notifications.assertNotificationOnTopicSentBranchNotSent(codeReview, codReviewStarter);
    }

    @Test
    public void deleteCodeReview_UserSubscribedToCodeReviewDoNotReceiveNotificationIfHeHimselfDeletedCodeReview()
        throws Exception{
        User codReviewStarter = Users.signUpAndSignIn();
        CodeReview codeReview = Topics.createCodeReview(new CodeReview()/*.withBranch(BRANCH_NAME)*/);
        Branches.unsubscribe(codeReview.getBranch());
        Topics.subscribe(codeReview);
        Topics.deleteTopic(codeReview);
        Branches.unsubscribeSafe(codeReview.getBranch());

        Notifications.assertNotificationOnTopicNotSentBranchNotSent(codeReview, codReviewStarter);
    }

    @Test
    public void deleteCodeReview_UserSubscribedToBranchDoNotReceiveNotificationIfHeHimselfDeletedCodeReview()
            throws Exception{
        User codReviewStarter = Users.signUpAndSignIn();
        CodeReview codeReview = Topics.createCodeReview(new CodeReview()/*.withBranch(BRANCH_NAME)*/);
        Branches.subscribe(codeReview.getBranch());
        Topics.unsubscribe(codeReview);
        Topics.deleteTopic(codeReview);
        Branches.unsubscribeSafe(codeReview.getBranch());

        Notifications.assertNotificationOnTopicNotSentBranchNotSent(codeReview, codReviewStarter);
    }

    @Test
    public void deleteCodeReview_UnsubscribedUserDoNotReceiveNotifications()
            throws Exception{
        User codReviewStarter = Users.signUpAndSignIn();
        CodeReview codeReview = Topics.createCodeReview(new CodeReview().withBranch(BRANCH_NAME));
        Topics.unsubscribe(codeReview);
        Branches.unsubscribe(codeReview.getBranch());
        Users.logout();

        User codReviewDeleter = Users.signUpAndSignIn();
        Branches.openBranch(codeReview.getBranch().getTitle());
        Topics.openTopicInCurrentBranch(100, codeReview.getTitle());
        Topics.deleteTopic(codeReview);
        Branches.unsubscribeSafe(codeReview.getBranch());

        Notifications.assertNotificationOnTopicNotSentBranchNotSent(codeReview, codReviewStarter);
    }

    @Test
    public void moveCodeReview_UserSubscribedToCodeReviewOnlyReceivesCodeReviewNotificationOnly()
        throws Exception{
        User codReviewStarter = Users.signUpAndSignIn();
        CodeReview codeReview = Topics.createCodeReview(new CodeReview().withBranch(BRANCH_NAME));
        Topics.subscribe(codeReview);
        Branches.unsubscribe(codeReview.getBranch());
        Users.logout();

        User codeReviewMover = Users.signUpAndSignIn();
        Branches.openBranch(codeReview.getBranch().getTitle());
        Topics.openTopicInCurrentBranch(100, codeReview.getTitle());
        Topics.moveTopic(codeReview, BRANCH_NAME_TO_MOVE_TOPIC_IN);
        Branches.unsubscribeSafe(codeReview.getBranch());

        Notifications.assertNotificationOnTopicSentBranchNotSent(codeReview, codReviewStarter);
    }

    @Test
    public void moveCodeReview_UserSubscribedToBranchOnlyDoNotReceiveNotification()
            throws Exception{
        User codReviewStarter = Users.signUpAndSignIn();
        CodeReview codeReview = Topics.createCodeReview(new CodeReview()/*.withBranch(BRANCH_NAME)*/);
        Topics.unsubscribe(codeReview);
        Branches.subscribe(codeReview.getBranch());
        Branch oldBranch = codeReview.getBranch();
        Users.logout();

        User codeReviewMover = Users.signUpAndSignIn();
        Branches.openBranch(codeReview.getBranch().getTitle());
        Topics.openTopicInCurrentBranch(100, codeReview.getTitle());
        Topics.moveTopic(codeReview, BRANCH_NAME_TO_MOVE_TOPIC_IN);
        Branches.unsubscribeSafe(codeReview.getBranch());
        Users.logout();

        Users.signIn(codReviewStarter);
        Branches.unsubscribeSafe(oldBranch);
        Branches.unsubscribeSafe(codeReview.getBranch());

        Notifications.assertNotificationOnTopicNotSentBranchNotSent(codeReview, codReviewStarter);
        Notifications.assertBranchNotificationNotSentTo(oldBranch, codReviewStarter);
    }

    @Test
    public void moveCodeReview_UserSubscribedBothToBranchAndCodReviewReceivesCodeReviewNotificationOnly()
            throws Exception{
        User codReviewStarter = Users.signUpAndSignIn();
        CodeReview codeReview = Topics.createCodeReview(new CodeReview()/*.withBranch(BRANCH_NAME)*/);
        Topics.subscribe(codeReview);
        Branches.subscribe(codeReview.getBranch());
        Branch oldBranch = codeReview.getBranch();
        Users.logout();

        User codeReviewMover = Users.signUpAndSignIn();
        Branches.openBranch(codeReview.getBranch().getTitle());
        Topics.openTopicInCurrentBranch(100, codeReview.getTitle());
        Topics.moveTopic(codeReview, BRANCH_NAME_TO_MOVE_TOPIC_IN);
        Branches.unsubscribeSafe(codeReview.getBranch());
        Users.logout();

        Users.signIn(codReviewStarter);
        Branches.unsubscribeSafe(oldBranch);
        Branches.unsubscribeSafe(codeReview.getBranch());

        Notifications.assertNotificationOnTopicSentBranchNotSent(codeReview, codReviewStarter);
        Notifications.assertBranchNotificationNotSentTo(oldBranch, codReviewStarter);
    }

    @Test
    public void moveCodeReview_UserSubscribedOnCodeReviewDoNotReceiveNotificationIfHeHimselfMovedCodeReview()
        throws Exception{
        User codeReviewStarter = Users.signUpAndSignIn();
        CodeReview codeReview = Topics.createCodeReview(new CodeReview().withBranch(BRANCH_NAME));
        Branch oldBranch = codeReview.getBranch();
        Branches.unsubscribe(codeReview.getBranch());
        Topics.subscribe(codeReview);
        Topics.moveTopic(codeReview, BRANCH_NAME_TO_MOVE_TOPIC_IN);
        Branches.unsubscribeSafe(codeReview.getBranch());

        Notifications.assertNotificationOnTopicNotSentBranchNotSent(codeReview, codeReviewStarter);
        Notifications.assertBranchNotificationNotSentTo(oldBranch, codReviewStarter);
    }

    @Test
    public void moveCodeReview_UnsubscribedUserDoNotReceiveNotifications()
            throws Exception{
        User codReviewStarter = Users.signUpAndSignIn();
        CodeReview codeReview = Topics.createCodeReview(new CodeReview().withBranch(BRANCH_NAME));
        Branch oldBranch = codeReview.getBranch();
        Topics.unsubscribe(codeReview);
        Branches.unsubscribe(codeReview.getBranch());
        Users.logout();

        User codeReviewMover = Users.signUpAndSignIn();
        Branches.openBranch(codeReview.getBranch().getTitle());
        Topics.openTopicInCurrentBranch(100, codeReview.getTitle());
        Topics.moveTopic(codeReview, BRANCH_NAME_TO_MOVE_TOPIC_IN);
        Branches.unsubscribeSafe(codeReview.getBranch());

        Notifications.assertNotificationOnTopicNotSentBranchNotSent(codeReview, codeReviewStarter);
        Notifications.assertBranchNotificationNotSentTo(oldBranch, codReviewStarter);
    }

    @Test
    public void editCodeReviewComment_UserSubscribedToCodeReviewOnlyDoNotReciveNotification()
            throws Exception {
        User codReviewStarter = Users.signUpAndSignIn();
        CodeReview codeReview = Topics.createCodeReview(new CodeReview().withBranch(BRANCH_NAME));
        Topics.subscribe(codeReview);
        Branches.unsubscribe(codeReview.getBranch());
        Users.logout();

        User codeReviewCommentEditor = Users.signUpAndSignIn();
        Branches.openBranch(codeReview.getBranch().getTitle());
        Topics.openTopicInCurrentBranch(100, codeReview.getTitle());
        CodeReviewComment codeReviewComment = new CodeReviewComment().withContent(randomAlphanumeric(100));
        Topics.leaveCodeReviewComment(codeReviewComment);
        codeReview.getPosts().get(0).setPostContent("Edited comment");
        //Branches.unsubscribeSafe(codeReview.getBranch());

        Notifications.assertNotificationOnTopicNotSentBranchNotSent(codeReview, codeReviewStarter);
    }

    @Test
    public void editCodeReviewComment_UserSubscribedToBranchOnlyDoNotReciveNotification()
            throws Exception {
        User codReviewStarter = Users.signUpAndSignIn();
        CodeReview codeReview = Topics.createCodeReview(new CodeReview().withBranch(BRANCH_NAME));
        Topics.unsubscribe(codeReview);
        Branches.subscribe(codeReview.getBranch());
        Users.logout();

        User codeReviewCommentEditor = Users.signUpAndSignIn();
        Branches.openBranch(codeReview.getBranch().getTitle());
        Topics.openTopicInCurrentBranch(100, codeReview.getTitle());
        CodeReviewComment codeReviewComment = new CodeReviewComment().withContent(randomAlphanumeric(100));
        Topics.leaveCodeReviewComment(codeReviewComment);
        codeReview.getPosts().get(0).setPostContent("Edited comment");
        //Branches.unsubscribeSafe(codeReview.getBranch());
        Users.logout();

        Users.signIn(codReviewStarter);
        Branches.unsubscribeSafe(codeReview.getBranch());

        Notifications.assertNotificationOnTopicNotSentBranchNotSent(codeReview, codeReviewStarter);
    }

    @Test
    public void deleteCodeReviewComment_UserSubscribedToCodeReviewOnlyReceivesCodeReviewNotificationOnly()
            throws Exception {
        User codReviewStarter = Users.signUpAndSignIn();
        CodeReview codeReview = Topics.createCodeReview(new CodeReview().withBranch(BRANCH_NAME));
        Topics.subscribe(codeReview);
        Branches.unsubscribe(codeReview.getBranch());
        Users.logout();

        User codeReviewCommentDelitor = Users.signUpAndSignIn();
        Branches.openBranch(codeReview.getBranch().getTitle());
        Topics.openTopicInCurrentBranch(100, codeReview.getTitle());
        CodeReviewComment codeReviewComment = new CodeReviewComment().withContent(randomAlphanumeric(100));
        Topics.leaveCodeReviewComment(codeReviewComment);
        codeReview.getPosts().remove(0);
        //Branches.unsubscribeSafe(codeReview.getBranch());

        Notifications.assertNotificationOnTopicSentBranchNotSent(codeReview, codReviewStarter);
    }

    @Test
    public void deleteCodeReviewComment_UserSubscribedToBranchOnlyDoNotReceiveNotifications()
            throws Exception{
        User codReviewStarter = Users.signUpAndSignIn();
        CodeReview codeReview = Topics.createCodeReview(new CodeReview().withBranch(BRANCH_NAME));
        Topics.unsubscribe(codeReview);
        Branches.subscribe(codeReview.getBranch());
        Users.logout();

        User codeReviewCommentDelitor = Users.signUpAndSignIn();
        Branches.openBranch(codeReview.getBranch().getTitle());
        Topics.openTopicInCurrentBranch(100, codeReview.getTitle());
        CodeReviewComment codeReviewComment = new CodeReviewComment().withContent(randomAlphanumeric(100));
        Topics.leaveCodeReviewComment(codeReviewComment);
        codeReview.getPosts().remove(0);
        //Branches.unsubscribeSafe(codeReview.getBranch());
        Users.logout();

        Users.signIn(codReviewStarter);
        Branches.unsubscribeSafe(codeReview.getBranch());

        Notifications.assertNotificationOnTopicNotSentBranchNotSent(codeReview, codeReviewStarter);
    }

    @Test
    public void deleteCodeReviewComment_UserSubscribedBothToBranchAndCodReviewReceivesCodeReviewNotificationOnly()
            throws Exception{
        User codReviewStarter = Users.signUpAndSignIn();
        CodeReview codeReview = Topics.createCodeReview(new CodeReview().withBranch(BRANCH_NAME));
        Topics.subscribe(codeReview);
        Branches.subscribe(codeReview.getBranch());
        Users.logout();

        User codeReviewCommentDelitor = Users.signUpAndSignIn();
        Branches.openBranch(codeReview.getBranch().getTitle());
        Topics.openTopicInCurrentBranch(100, codeReview.getTitle());
        CodeReviewComment codeReviewComment = new CodeReviewComment().withContent(randomAlphanumeric(100));
        Topics.leaveCodeReviewComment(codeReviewComment);
        codeReview.getPosts().remove(0);
        //Branches.unsubscribeSafe(codeReview.getBranch());
        Users.logout();

        Users.signIn(codReviewStarter);
        Branches.unsubscribeSafe(codeReview.getBranch());

        Notifications.assertNotificationOnTopicSentBranchNotSent(codeReview, codReviewStarter);
    }

    @Test
    public void deleteCodeReviewComment_UserSubscribedToCodeReviewOnlyDoNotReceiveNotificationsIfHimselfDeletesComment()
            throws Exception {
        User codReviewStarter = Users.signUpAndSignIn();
        CodeReview codeReview = Topics.createCodeReview(new CodeReview().withBranch(BRANCH_NAME));
        Topics.subscribe(codeReview);
        Branches.unsubscribe(codeReview.getBranch());
        Topics.openTopicInCurrentBranch(100, codeReview.getTitle());
        CodeReviewComment codeReviewComment = new CodeReviewComment().withContent(randomAlphanumeric(100));
        Topics.leaveCodeReviewComment(codeReviewComment);
        codeReview.getPosts().remove(0);
        Branches.unsubscribeSafe(codeReview.getBranch());

        Notifications.assertNotificationOnTopicNotSentBranchNotSent(codeReview, codeReviewStarter);
    }
}
