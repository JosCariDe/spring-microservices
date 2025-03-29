// init-mongo.js
db = db.getSiblingDB('productdb');

db.products.insertMany([
    {
        "_id": UUID("550e8400-e29b-41d4-a716-446655440002"),
        "name": "Laptop",
        "price": 999.99,
        "category": "Electronics"
    },
    {
        "_id": UUID("550e8400-e29b-41d4-a716-446655440003"),
        "name": "Headphones",
        "price": 49.99,
        "category": "Accessories"
    },
    {
        "_id": UUID("550e8400-e29b-41d4-a716-446655440006"),
        "name": "Smartphone",
        "price": 699.99,
        "category": "Electronics"
    }
]);