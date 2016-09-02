## A simple http reacitve stream lib

### What it is?

The library provide two type of HTTP request method, 'GET', 'POST', to send a series
of request to the endpoint, then return the result to user caller.

It also provide the simple option to enable recovery by saving the request not success
into the redis service, then after the request done, user can retrieve them to try again.

