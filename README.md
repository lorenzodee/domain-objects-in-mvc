# Dealing with Domain Objects in Spring MVC

This contains sample code that shows how to deal with domain object in Spring MVC (also discussed in my blog post). Specifically, the code contained in here deals with the following cases:

1. No setter for generated ID field (i.e. the generated ID field has a getter but no setter)
2. No default constructor (e.g. no public zero-arguments constructor)
3. Domain entity with child entities (e.g. child entities are not exposed as a modifiable list)

## No Setter for Generated ID Field

In this case, the domain entity has a generated ID field that is read-only. It does not provide a setter method to set the ID field. Keeping the generated ID field read-only is considered to be a good practice when using JPA.

```java
@Entity
... class SampleEntity {
    ...
    @Id @GeneratedValue(...)
    private Long id;

    public Long getId() { return id; }
    // but no setter method for the "id" field
    
    // other fields have getters and setters
}
```

If we need a controller to provide a web-based interface to that particular entity, Spring MVC would need to *bind* request parameters to that entity. Spring binds request parameters by name. If we handle a `PUT` request, the problem is in allowing Spring to bind the "id" parameter.

```java
@Controller
@RequestMapping(...)
... class ... {
    ...
    @PutMapping("/{id}")
    public String update(@PathVariable Long id, ... SampleEntity entity, ...) {
        // At this point, Spring MVC would have created
        // an instance of SampleEntity using its default
        // constructor. But the "id" field would NOT have
        // been set!
        ...
        sampleEntityRepository.save(entity);
            // and this ends up being an INSERT, not an UPDATE *frown*
        return ...;
    }
}
```

You might think that it would be necessary to either add a setter method (`setId`) to the entity class. Or, in the controller class, add code that would retrieve the existing entity by ID, and manually merge the entities. These two approaches will work. **But there's an easier way!**

We use a `@ModelAttribute` method in the controller. Spring MVC will invoke this method *prior* to the mapped request handler. This is a convenient way to retrieve the existing entity, and Spring will bind the request parameters to it. The revised controller code would look something like:

```java
@Controller
@RequestMapping(...)
... class ... {
    ...
    @ModelAttribute
    public SampleEntity populateModel(...,
            @PathVariable(required=false) Long id, ...) {
        if (id != null) {
            return sampleEntityRepository.findById(id).orElseThrow(...);
        }
        ...
        return null;
    }

    @PutMapping("/{id}")
    public String update(... SampleEntity entity, ...) {
        ...
        sampleEntityRepository.save(entity);
        return ...;
    }
}
```

Please see [GeneratedIdEntity](src/main/java/domainobjectsmvc/domain/model/GeneratedIdEntity.java), [GeneratedIdEntitiesController](src/main/java/domainobjectsmvc/webmvc/GeneratedIdEntitiesController.java) and [GeneratedIdEntitiesControllerTests](src/test/java/domainobjectsmvc/webmvc/GeneratedIdEntitiesControllerTests.java) for more details.

## No Default Constructor

This time, the domain entity does not provide a `public` default constructor. In other words, it does not provide a `public` zero-arguments constructor.

```java
@Entity
... class SampleEntity {
    ...    
    // no public default constructor

    public SampleEntity(... arg1, ... arg2) {...}

    // other operations

    protected SampleEntity() {
        // as required by ORM/JPA, not by design
    }
}
```

When dealing with this type of domain object, some developers would add a public default constructor to the domain entity. But this may end up violating some of the domain object's invariants.

JPA requires a `public` or `protected` zero-arguments constructor. The sample entity provides a `protected` one. Can Spring MVC use the `protected` zero-arguments constructor?

No. Spring MVC only uses the public default constructor.

So, is there an easy way to do this? Yes, there is! In the `@ModelAttribute` method, we use request parameters as arguments when calling the public constructor. Note that this will require some type conversions, and error handling when the conversion fails.

```java
@Controller
@RequestMapping(...)
... class ... {
    ...
    @ModelAttribute
    public SampleEntity populateModel(
            HttpMethod httpMethod,
            @PathVariable(required=false) Long id,
            @RequestParam Map<String, String> params) {
        if (id != null) {
            return sampleEntityRepository.findById(id).orElseThrow(...);
        }
        if ((httpMethod == HttpMethod.GET && ...)
                   || httpMethod == HttpMethod.POST) {
            return new SampleEntity(params.get("arg1"), params.get("arg2"));
        }
        return null;
    }

    @PostMapping
    public String save(... SampleEntity entity, ...) {
        ...
        sampleEntityRepository.save(entity);
        return ...;
    }
}
```

Please see [Account](src/main/java/domainobjectsmvc/domain/model/Account.java), [AccountsController](src/main/java/domainobjectsmvc/webmvc/AccountsController.java) and [AccountsControllerTests](src/test/java/domainobjectsmvc/webmvc/AccountsControllerTests.java) for more details.

The astute reader would have noticed that the request parameters would need type conversion and error handling. A better way is to use another class for Spring MVC to bind request parameters, and then use it to apply changes to the domain object. This approach is also discussed in the next section.

## Domain Entity with Child Entities

Some domain entities have child entities (like aggregate roots). And these child objects are not necessarily exposed as a modifiable list. This becomes another challenge when used within Spring MVC. That's because when Spring MVC binds request parameters with indexed property names, it expects the child objects to be exposed as a modifiable list. So, the following sample entity will cause Spring MVC to fail to bind.

```java
... class Order {
    private Map<ProductId, OrderItem> items;
    ...
    public void addItem(int quantity, ProductId productId) {...}

    public void removeItem(ProductId productId) {...}
	
    public Collection<OrderItem> getItems() {
        return Collections.unmodifiableCollection(items.values());
    }
}

... class OrderItem {
    private int quantity;
    ...
    // no public default constructor
    public int getQuantity() {...}
    public void setQuantity(int quantity) {...}
}
```

When Spring MVC gets a "items[0].quantity" request parameter, it will expect that `getItems()` returns a `List`. If it returns `null`, it will create a list and call `setItems()`. Then attempt to call `get(0)` (or call `add()` until the desired index is reached, and expect a public default constructor for list elements). Then, it will call `setQuantity` passing in the converted request parameter value.

Obviously, the domain entity will need a significant overhaul just to make Spring MVC happy. But there is an easier way!

Instead of *fighting* against persistence mapping and web frameworks, we define another type that is JavaBean-like to make binding easy in Spring MVC. This type knows how to use the domain entity and its children. But the domain entity does not need to know about this type. It looks something like this:

```java
... class OrderForm {
    public static OrderForm fromDomainEntity(Order order) {...}
    private final Order order;
    private List<OrderFormItem> formItems;
    public OrderForm(Order order) {...}
    public List<OrderFormItem> getItems() {...}
    public void setItems(List<OrderFormItem> items) {...}
    public Order toDomainEntity() {...}
}
... class OrderFormItem {
    // public default constructor
    // getters and setters
}
```

Specifically, `OrderForm` knows about the `Order` and `OrderItem` domain objects, but the `Order` and `OrderItem` domain objects do not know about `OrderForm`.

So, how do we use these in a controller?

```java
@Controller
@RequestMapping(...)
... class ... {
    ...
    @ModelAttribute
    public OrderForm populateModel(
            HttpMethod httpMethod,
            @PathVariable(required=false) Long id,
            @RequestParam Map<String, String> params) {
        if (id != null) {
            Order order = orderRepository.findById(id).orElseThrow(...);
            return OrderForm.fromDomainEntity(order);
        }
        if ((httpMethod == HttpMethod.GET && ...)
                   || httpMethod == HttpMethod.POST) {
            return OrderForm.fromDomainEntity(new Order());
        }
        return null;
    }

    @PostMapping
    public String save(... OrderForm orderForm, ...) {
        ...
        orderRepository.save(orderForm.toDomainEntity());
        return ...;
    }

    @PutMapping("/{id}")
    public String update(... OrderForm orderForm, ...) {
        ...
        orderRepository.save(orderForm.toDomainEntity());
        return ...;
    }

}
```

Please see [Order](src/main/java/domainobjectsmvc/domain/model/Order.java), [OrdersController](src/main/java/domainobjectsmvc/webmvc/OrdersController.java) and [OrdersControllerTests](src/test/java/domainobjectsmvc/webmvc/OrdersControllerTests.java) for more details.

