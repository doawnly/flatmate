CREATE TABLE households (
                            id SERIAL PRIMARY KEY,
                            name VARCHAR(100) NOT NULL
);

CREATE TABLE users (
                       id SERIAL PRIMARY KEY,
                       name VARCHAR(100) NOT NULL,
                       email VARCHAR(255) NOT NULL,
                       household_id INTEGER REFERENCES households(id)
);

CREATE TABLE expenses (
                          id SERIAL PRIMARY KEY,
                          description VARCHAR(255) NOT NULL,
                          amount NUMERIC(10, 2) NOT NULL,
                          paid_by INTEGER NOT NULL REFERENCES users(id),
                          household_id INTEGER NOT NULL REFERENCES households(id)
);

CREATE TABLE expense_splits (
                                id SERIAL PRIMARY KEY,
                                expense_id INTEGER NOT NULL REFERENCES expenses(id),
                                user_id INTEGER NOT NULL REFERENCES users(id),
                                amount_owed NUMERIC(10, 2) NOT NULL
);