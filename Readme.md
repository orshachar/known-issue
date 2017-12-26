# known-issue

Add a @KnownIssue annotation for TestNg tests that already have a related bug on Jira, 
and they will be skipped until it is resolved. 

## Installation

1. Add This module to your project, or add the maven dependency(T.B.D) to your pom.xml
2. Create a jira.properties file under your /resources folder, add the following properties:
   
   - jira.url=https://example.atlassian.net
   - jira.user=jira_username
   - jira.password=jira_password
   
   Or use the properties file here as a template
   
   ### Note  
   only Basic HTTP Auth is currently supported, OAuth support will be added soon.
## Usage

For a test that fails and has bug on Jira, you can now add a @KnownIssue annotation
before the test, with the Jira ticket id. While the bug is on an Open/ToDo status
the test will be skipped. If the bug was resolved and the test passes, it will
now fail and indicate that you need to close the ticket and remove the annotation.
otherwise, it will still fail. 

See the following example for adding the annotation: 


    @KnownIssue(jiraTicket = "KNOW-1")
    @Test
    public void testFailedWithOpenIssue() {
        assertThat(1, equalTo(2));

    }
    
This test will fail by defintion, but will be skipped if "KNOW-1" is in status "Resolved"    

## Documentation

| Issue      | Current Status           | Final Status  |
| ------------- |:-------------:| -----:|
| None      | Failed | Failed |
| None      | Passed | Passed |
| Open     | Failed      |   Skipped |
| Resolved | Passed      |    Failed - close ticket and remove anotation |
| Resolved | Failed      |    Failed - reopen ticket
| Closed | Failed      |    Failed - reopen ticket
| Closed | Passed      |    Failed - remove annotation


## Contributing

  * Fork it! 
  * Create your feature branch: `git checkout -b my-new-feature` 
  * Commit your changes: `git commit -am 'Add some feature'` 
  * Push to the branch: `git push origin my-new-feature`
  * Submit a pull request!

## License

GNU General Public License v3.0