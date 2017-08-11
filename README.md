# Code Less
##### Tools for the busy Java developer

## Highlights

* Help remove boilerplate code from Java programs
* Make trivial things simple, not so trivial not so simple
* Tune the behavior of any component
* Java interfaces, but implementations are in any JVM language (currently Scala and Java)
* Test with Scala Spec and JUnit

## What's in the package?

### Decorators

Imagine, you have a list of 10,000 objects which you need to run against some external API. So your
Java code may look as simple as

```Java

List<Response> result = api.process(myItems);

``` 
but there are issues. 

    * The API throws this ugly `ApiException` which you have to catch.
    * The API unstable, so you want to retry the call multiple times before giving up.
    * The API imposes a request size limit of 500, so you have to make multiple calls to process you list
    (and retry each one of them!).
    
Decorators to the rescue! Wrap you Java call as:

```Java
    List<Request> myItems = ... ;
    
    // batch by maximum request size
    // retry 3 times every minute
    // convert checked exceptions into runtime exceptions
    
    List<Response> result =
        batched(API.MAX_REQUEST_SIZE,
            retried(3, 1000 * 60,
                unchecked((List<Request> list) -> api.process(list)))).apply(myItems);
```

### Configurable Objects

Every object is configurable, and there is a default configuration for each object!

Suppose, you need to convert your customer data to JSON. Here is your customer:

```Java
    Customer customer = new Customer("John Doe", new String[] {"212-111-9999"});
```

JsonMapper will help.  It's use may be as simple as

```Java
    JsonMapper mapper = JsonMapper.getJsonMapper();
    mapper.toJson(customer);

    // produces
    // {"name":"John Doe","phones":["212-111-9999"]}
```

or

```Java
    JsonMapper mapper = JsonMapper.getJsonMapper(false)
        .with(JsonMapper.prettyPrinting, true)
        .with(JsonMapper.unwrapSingleElementArrays, true)
        .with(JsonMapper.visibility, Visibility.METHOD)
        .with(JsonMapper.wrapRootValue, true)
        .locked();
    mapper.toJson(customer);    
        
    // produces
    /*
    {
      "Customer" : {
        "name" : "John Doe",
        "phones" : "212-111-9999"
      }
    }
    */
```

"**Configurable objects**" framework is builder pattern on steroids!


### Serializers and other file utilities

Serializing/de-serializing objects to/from files/streams is easy.

```Java
    // process customers stored in JSON format in a file
    try (ObjectIterator<Customer> iter = JsonIterator.fromFile(myFile, Customer.class)) {
        iter.forEachBatch(500, customers -> {
            // save them to DB in batches of 500
        });
    }
```

or

```Java
    List<Customer> myCustomers = ...
    
    // dump customer list to a file using Java serialization
    try (ObjectWriter<Customer> writer = JavaWriter.toFile(myFile)) {
        writer.write(myCustomers);
    }
``` 

There are also **serializers**, which are pairs of an object reader and an object writer sharing the same
configuration settings. Use them when you need to read objects from a file, process them somehow, and
put the results of processing back into a different file. For example,

```Java
    // generate a file of customer names for those customers who don't have phones
    try (Serializer<Customer, String> s = Serializer.jsonSerializer(inFile, outFile, Customer.class)) {
        s.filterAndMap(c -> c.getPhones() == null, Customer::getName);
    }
```

this snippet will actually write customer names (which are strings) in JSON format.  If you'd like to
dump them as plain strings you would do

```Java
    try (Serializer<Customer, String> s = Serializer.serializer(
            inFile, outFile, 
            SerializationType.JSON,              // input serialization type
            SerializationType.STRING,            // output serialization type
            Optional.of(Customer.class),         // object class (needed by JSON iterator)
            Optional.of(Configurable.empty())))  // no additional configuration settins necessary 
    {
        s.filterAndMap(c -> c.getPhones() == null, Customer::getName);
    }
``

#### File sorter

File sorter makes use of **serializers** to sort files of any size.  File sorter may sort them in 
memory or it may sort them by using "external merge sort" if files are very large. There is a file size
threshold (configurable!) which is used by the file sorter to switch from "in-memory" sorting to
"external merge" sorting.

Additionally, file sorter can:

    * remove duplicates while sorting
    * skip one ore more header lines (when for example sorting CSV files)


The use file sorter may be as simple as:

```Java
    FileSorter.sort(myFile);
```

or as complex as :

```Java
    FileSorter<Customer> fileSorter = FileSorter.getFileSorter(
                inFile,                      // input file 
                outFile,                     // output (sorted) file
                SerializationType.JSON,      // what is the serialization scheme (file format)
                Customer.class,              // object class (needed by JSONMapper)
                Comparator.reverseOrder(),   // sort in descending order. 
                true);                       // remove duplicates
    fileSorter.sort();
```

### More detailed list of features

* [Decorators](../core/src/main/java/cl/core/decorator/package-info.java)
* [Decorators](../core/src/main/java/cl/core/decorator)


 