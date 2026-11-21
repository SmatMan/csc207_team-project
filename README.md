# Project Name

## Team Information

[Project Google Docs Link](https://docs.google.com/document/d/1_SdBBDG2W2mms3sfqU-FKWBU6JN7IfM8zoO38baLJZI/edit?usp=sharing)

### Team Members

- Dion Machado 

- Alex Davydenko

- Arjun Mehandiratta

- Michael Bayato

- Saihaan Selim 

---

## Project Description

### Domain

This project is an application where you can input ingredients that you have in your pantry, fridge, or house, and you can get custom recipes based on preferences such as dish type, cultural, dietary restrictions, and more. The application also allows you to store, favourite and delete recipes. 

---

## User Stories

### User Story 1

As a student with a half-empty fridge, I want to add, edit and remove the ingredients I have, so that the app knows what I can cook without me retyping everything each time.

*Expands into use cases: Creating items in, and modifying Pantry and Ingredient entities*

### User Story 2

As someone deciding what to cook tonight, I want recipes ranked by how few ingredients they need so I can cook without having to do a grocery run.

*Expands into use case: findByIngredients API call to Spoonacular*

### User Story 3

As a vegetarian who is lactose intolerant, I want to set my diet and intolerances once so I'm never shown recipes I can't eat.

*Expands into use case: creating a `UserProfile` that saves `Intolerance`*
### User Story 4

As someone who found a recipe I liked, I want to favourite it and delete it later, so that I can return to meals I know work for me instead of searching again.

*Expands into use cases: Storing recipes in lists, deleting recipes in lists*
### User Story 5

I have a lot of perishable ingredients in my fridge. I want to prioritize recipes that use up my expiring ingredients.

*Expands into use cases: Mark specific ingredients as expiring; Sort recipes by ones that use up the most expiring ingredients*

### User Story 6

I don't want to go through the effort of adding all the ingredients, I just want to use the app as a recipe searcher.

*Expands into use case: Search bar shows "search recipes" when no ingredients are in pantry*

---

## Use Cases

### Use Case 1
Goal: Manage a `Pantry` entity storing Ingredient entities.

Lead: Arjun
### Use Case 2

Goal: Filter recipes from spoonacular with a `findByIngredients` API call passing `Ingredient` entities in `Pantry` entity.

Lead: Dio
### Use Case 3

Goal: Creating `UserProfile` entities that save both `Ingredient` and `Intolerance` entities (`Intolerance` entity holds list of `Ingredient`'s that get excluded)

> **Note:** the restriction categories, ingredients and diet presets in `src/main/resources/restrictions.json` are a **placeholder**. More or fewer will be added once Dion's Spoonacular API calls produce the full list of ingredients, so the names match what the API uses.

### Use Case 4

Goal: Storing/saving/deleting `Recipe` entities in `RecipeList`

### Use Case 5

Goal: Creating/Deleting `RecipeList` entities

### Use Case 6

Goal: Mark `Ingredient` as expiring to prioritize

Lead: Arjun

---

## Minimal Viable Product (MVP)

| Team Member | MVP Use Case           |
| ----------- | ---------------------- |
| Arjun       | Use Case 1, Use Case 6 |
| Dio         | Use Case 2             |
| Alex        | Use Case 3; produce a front end independent from the data (theme, sidebar, views) |
|             |                        |
|             |                        |


---

## Entities

### Entity 1
### Entity 2

(Add additional entities as needed.)

---

## API Usage

**API Name:**

**Documentation:** [Link](https://spoonacular.com/food-api)

**How it is used in the program:**

---

## Design Diagrams

### UML Class Diagram

### Sequence Diagrams

---

## How to Run

[Instructions for running the program.]

---

## Notes

[Any additional project information.]
Annotations
