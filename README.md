

# Java concurrent & time-based rate-limiting library based on token-bucket algorithm and  semaphores.

[![Licence](https://img.shields.io/hexpm/l/plug.svg)](https://github.com/milos01/rate-limter-spring-boot-starter/blob/main/LICENSE)
## About
This Spring Boot starter project is meant to be used to overcome issues that I had when trying to use a combined approach of using both concurrent and time-based throttling inside Spring Boot applications. It's built on top of [Bucket4J](https://github.com/vladimir-bukhtoyarov/bucket4j)  (token-bucket algorithm implementation) library and semaphores which enables concurrent rate limiting. The idea was seamlessly plugging this library in any new or ongoing project and not write a whole bunch of configuration, logic adaptation, and so on. 

For now, the library fully supports Redis integration for both concurrent and time-based throttling, and if you want to use just time-based limiting then the Hazelcast is additionally supported. You can choose which type of throttling you want to use: 
*  both concurrent and time-based 
*  only concurrent 
*  only time based

As you can notice this library is primarily meant for using it inside distributed systems, so all new features will be towards that like support for different In-memory data grids (I will also make an option to not use distributed caching in one of the future releases).

## Usage
First of all, you need to include this library along with the desired In-memory grid database (Redis or Hazelcast). Here is an example using Maven.

```xml
<dependency>
    <groupId>com.github.milos01</groupId>
    <artifactId>rate-limiter-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>

<dependency>  
	<groupId>org.redisson</groupId>  
	<artifactId>redisson</artifactId>  
	<version>3.13.5</version>  
</dependency>

<!-- or -->
<dependency>  
	<groupId>com.hazelcast</groupId>  
	<artifactId>hazelcast</artifactId>  
	<version>3.12.8</version>  
</dependency>
``` 
after this, two more steps need to be followed. The next one is to detect which endpoint shout be throttled and to annotate those methods. There are three ways of decorating with @RateLimit annotation.

 1. If we use just @RateLimit annotation then the method name will be used alongside with classpath and be uniquely defined

```java
package com.github.milos01;

public class ItemConroller {
	@RateLimter
	public void getItems(final int id) {
		//genreated limit name will be
		//com.github.milos01.ItemController.getItem
	}
}
```
 2. You can also define a specific name to each endpoint so that he can manually manage all buckets and cache to be shared between multiple endpoints
```java
package com.github.milos01;

public class ItemConroller {
	@RateLimter("items")
	public void getItems(final int id) {
		//genreated limit name will be "items"
	}
}
```
 3. Similarly to the second solution, we can use parameters to assign a name to endpoint
```java
package com.github.milos01;

public class ItemConroller {
	@RateLimter(name = "#{@limitConfig.getItems()}")
	public void getItems(final int id) {
		//genreated limit name will be from specified config 
	}
}
```
I was constantly stating "endpoint" but the thing that this annotation could be placed anywhere in business logic and it will be executed before the annotated method itself. After this step, nothing will happen yet, because we need to configure limitations for methods that we annotated. Here is one of the examples:

```yaml
rate-limiter:  
  name: redisson-limit  
  provider: jcache 
  limitConfigs:  
    - name: default
      limits:  
        - amount: 5  
          unit: Minutes  
          type: CONCURRENT  
        - amount: 10  
          unit: Minutes  
          type: TIME
```
Here we defined default limit configuration which will be applied to all annotated methods. If we want to specify a limit to the exact method then we need to add an additional property called ```limits``` like it the example below.

```yaml
rate-limiter:  
  name: redisson-limit  
  provider: jcache
  limits:
    com.github.milos01.ItemController.getItem: customLimitName 
  limitConfigs:  
    - name: default
      limits:  
        - amount: 5  
          unit: Minutes  
          type: CONCURRENT  
        - amount: 10  
          unit: Minutes  
          type: TIME
    - name: customLimitName
      limits:
        - amount: 120  
          unit: Minutes  
          type: TIME
```
In a configuration like this one, just the ones that we specified inside limits parameter will be delegated to custom-defined limit whereas all others will be redirected to the default limit.
## License
Copyright 2020 Miloš Andrić
Licensed under the Apache Software License, Version 2.0: <http://www.apache.org/licenses/LICENSE-2.0>.

## Have a question?
Feel free to ask:
* Linkedin private message [https://www.linkedin.com/in/milosandric](https://www.linkedin.com/in/milosandric)
* Email milosa942@gmail.com