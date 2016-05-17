skltp-agp
=========

This branch is specially created to fix VP 007 problem i SKLTP. This occurs when the consumers don't have access to producer system and end up throwing VP 007 error code in VP. In the first hand the information should have been filtered out by EI FindContent request but since it's complex to solve at this level due to lack of required data to do this. It is infact delegated to AgP Core.

See confluence:
https://skl-tp.atlassian.net/wiki/pages/viewpage.action?pageId=51347509

Jira Ticket:

https://skl-tp.atlassian.net/browse/SKLTP-762

About AgP Core component:

agp is the core component for building an skltp aggregating service.

An aggregating service provides one incoming interface for a consumer to invoke. The call is then routed to all appropriate producers. The service then aggregates the individual responses to one single response which is returne to the consumer.

Note that the agp component is bound to a specific version of the [skltp platform](https://skl-tp.atlassian.net/wiki/display/SKLTP/Release+information)

More information can be found on the wiki 
* [Aggregeringsplattform - arkitekturbeskrivning (SAD)](https://skl-tp.atlassian.net/wiki/pages/viewpage.action?pageId=1081368) 
* [Aggregerande Tjänster](https://skl-tp.atlassian.net/wiki/pages/viewpage.action?pageId=6160395)
