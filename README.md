# TransactionalMongo

**TransactionalMongo** provides **JPA-like dirty checking for Spring Data MongoDB**.  
It allows you to update entities without explicitly calling `save()`. Just load your entity, modify its fields, and changes will be automatically detected and persisted at the end of the method.

---

## Features

- ✅ **Dirty Checking** for MongoDB entities (similar to JPA/Hibernate).
- ✅ Works with plain POJOs annotated as MongoDB documents.
- ✅ No need to call `save()` manually — only update the fields you want.
- ✅ Optional timestamp support (`updatedAt`) via the `LastModifiedDate` interface.

---

## Installation

Add the dependency from Maven Central:

```gradle
dependencies {
    implementation "io.github.kamillcream:transactional-mongo:1.0.9.2"
}
```
---
## Usage

Annotate service methods with @TransactionalMongo
```
@Service
public class UserService {
    private final MongoEntityLoader loader;

    public UserService(MongoEntityLoader loader) {
        this.loader = loader;
    }

    @TransactionalMongo
    public void updateUserName(String id, String newName) {
        User user = loader.findById(id, User.class);
        if (user == null) throw new RuntimeException("User not found");
        user.setName(newName);
    }
}
```
- No repository.save() is required.
- The library will detect changes and persist them automatically.
- You must load the target entity using MongoEntityLoader instead of the Repository.


