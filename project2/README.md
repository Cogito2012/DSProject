# Instructions

### 1. Compile and run the EventManager.
Open a terminal and run the `compile.sh` file with specified PORT number.
```
bash ./compile.sh 6780
```

### 2. Start the Publisher client.
Open a terminal and run the following command:
```
java edu.rit.cs.PubClient localhost:6780
```

### 3 Perform actions of a publisher.
Do operations as intructed in the terminal of Publisher client. The followings are what have been printed in my terminal
```
================ Please choose [ID] of each following operations =====================
[0]: Login/Register my account.
[1]: Advertise a topic.
[2]: Publish an event.
[3]: Exit.
> 0
Enter your name:
Wentao Bao
Enter your password:
123456
Connected!
Get response from server: Login in successfully.
================ Please choose [ID] of each following operations =====================
[0]: Login/Register my account.
[1]: Advertise a topic.
[2]: Publish an event.
[3]: Exit.
> 1
Enter the topic content: 
basketball
Enter the keywords for this topic. Keywords should be a single line separated by whitespace:
crash celebrity
================ Please choose [ID] of each following operations =====================
[0]: Login/Register my account.
[1]: Advertise a topic.
[2]: Publish an event.
[3]: Exit.
> Topic is successfully stored in server!
1
Enter the topic content: 
disease
Enter the keywords for this topic. Keywords should be a single line separated by whitespace:
coronavirus china
================ Please choose [ID] of each following operations =====================
[0]: Login/Register my account.
[1]: Advertise a topic.
[2]: Publish an event.
[3]: Exit.
> Topic is successfully stored in server!
2
Enter the name your event topic:
disease
Enter event title:
wuhan2020
Enter content for this event. Leave a blank line to finish:
This is not a peaceful year!
Hope everything is good.

================ Please choose [ID] of each following operations =====================
[0]: Login/Register my account.
[1]: Advertise a topic.
[2]: Publish an event.
[3]: Exit.
> Event is successfully stored in server!
3

```

### 4 Perform actions of a subscriber.
Do operations as intructed in the terminal of Publisher client. 

Subscribe function is completed.

### Todo List

 - Subscriber Client's other operations.
 - Database operation: add/remove/retrieve for both topics and events.
 - Docker and Docker-compose execution.
