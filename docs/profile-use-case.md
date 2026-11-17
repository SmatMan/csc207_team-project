# Use Case 3: Create User Profile


Lead: Saihaan Selim

# The Build 

I’m building the flow where a user creates a named profile and saves the ingredients they want to avoid. The user picks a category from a list, such as Dairy, then selects the specific ingredients to exclude. They can select milk under Dairy and leave butter unselected. They can also create a profile with no restrictions and use it for regular recipe searches.

This use case covers creation and the first save. Profile editing, deletion, diet presets, and saved or favourited recipes are separate use cases.

How creation works
Validate the profile name. The interactor rejects a missing or blank name and trims surrounding whitespace. It lowercases the trimmed name with Locale.ROOT for the uniqueness check, so Home and home count as the same name. The profile still displays as Home.
Validate the selections. Category names come from the catalogue. The interactor checks that each submitted category exists, appears once, and contains every ingredient selected under it. Each Intolerance needs at least one ingredient. Duplicates within one list are rejected using Ingredient equality, so Milk and milk count as one ingredient. Two different categories can contain the same ingredient.
Build the profile. The interactor builds each Intolerance from its selected ingredients, creates the UserProfile, and calls exclude() for each selection. The profile model stores category membership separately from active exclusions, so building an Intolerance does not mark its ingredients as excluded.
Save and report. The data access layer checks name availability and saves the profile in one operation. On success, the presenter takes the display name from profile.getName(). A duplicate name produces a message asking the user to choose another name. A storage failure produces a message asking the user to try saving again.

## Java interfaces and data

### UserProfile

```java
UserProfile(String name, List<Intolerance> intolerances)
```

Stores the trimmed display name and a defensive copy of the intolerances. `getName()` returns the display name.

### Intolerance

```java
Intolerance(String name, List<Ingredient> ingredients)
```

Stores one group and validates its name and ingredient list.

### CreateProfileInputData

```java
CreateProfileInputData(String profileName, List<IntoleranceInputData> intolerances)
IntoleranceInputData(String categoryName, List<String> ingredientNames)
```

Carries the submitted name and selections. Each `IntoleranceInputData` carries one category and its selected ingredients.

### CreateProfileInputBoundary

```java
void execute(CreateProfileInputData inputData)
```

`CreateProfileInteractor` implements it.

### IntoleranceCatalogue

```java
List<Intolerance> getAvailableIntolerances()
```

Lets the interactor look up categories and their ingredients.

### CreateProfileDataAccessInterface

```java
boolean saveIfNameAvailable(String normalizedName, UserProfile profile) throws ProfileStorageException
```

Returns true after a successful save and false if the name is taken. A storage failure throws the checked exception.

### CreateProfileOutputBoundary

```java
void prepareSuccessView(CreateProfileOutputData outputData)
void prepareFailView(String message)
```

The success data is created with `CreateProfileOutputData(String profileName)`.