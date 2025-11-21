CREATE DATABASE IF NOT EXISTS mtg_commander_db;
USE mtg_commander_db;

-- Player Record Management
CREATE TABLE player (
    player_id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    city_address VARCHAR(100),
    age INT,
    UNIQUE (first_name, last_name)
);

-- Card Record Management
CREATE TABLE card (
    card_id INT AUTO_INCREMENT PRIMARY KEY,
    card_name VARCHAR(100) NOT NULL,
    card_mana_cost VARCHAR(50),
    card_type VARCHAR(50),
    card_subtype VARCHAR(50),
    card_power VARCHAR(10),
    card_toughness VARCHAR(10),
    card_text TEXT,
    card_edition VARCHAR(50),
    card_status ENUM('Legal', 'Banned', 'Game Changer') DEFAULT 'Legal'
);

-- Deck Details Management
CREATE TABLE deck (
    deck_id INT AUTO_INCREMENT PRIMARY KEY,
    deck_name VARCHAR(100) NOT NULL,
    player_id INT NOT NULL,
    commander_card_id INT,
    bracket_info VARCHAR(50),
    validity ENUM('Valid', 'Invalid') DEFAULT 'Valid',
    description TEXT,
    FOREIGN KEY (player_id) REFERENCES player(player_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    FOREIGN KEY (commander_card_id) REFERENCES card(card_id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
);

--  Deck Cards Management
CREATE TABLE deck_cards (
    deck_id INT,
    card_id INT,
    card_name VARCHAR(100),
    quantity INT DEFAULT 1,
    is_commander BOOLEAN DEFAULT FALSE,
    is_game_changer BOOLEAN DEFAULT FALSE,
    card_status ENUM('In Deck', 'Out of Deck') DEFAULT 'In Deck',
    PRIMARY KEY (deck_id, card_id),
    FOREIGN KEY (deck_id) REFERENCES deck(deck_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    FOREIGN KEY (card_id) REFERENCES card(card_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- Borrow Request Management
CREATE TABLE borrow_request (
    borrow_code INT AUTO_INCREMENT PRIMARY KEY,
    player_id INT NOT NULL,
    deck_id INT NOT NULL,
    borrow_type ENUM('Wait', 'Immediate') DEFAULT 'Immediate',
    request_date DATE NOT NULL,
    due_date DATE,
    return_date DATE,
    status ENUM('Pending', 'Approved', 'Returned', 'Overdue', 'Cancelled') DEFAULT 'Pending',
    FOREIGN KEY (player_id) REFERENCES player(player_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    FOREIGN KEY (deck_id) REFERENCES deck(deck_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- Card Usage Frequency Report
CREATE VIEW card_usage_frequency AS
SELECT 
    c.card_id,
    c.card_name,
    COUNT(dc.deck_id) AS decks_included
FROM card c
LEFT JOIN deck_cards dc ON c.card_id = dc.card_id
GROUP BY c.card_id
ORDER BY decks_included DESC;

-- Deck Usage Frequency Report
CREATE VIEW deck_usage_frequency AS
SELECT 
    d.deck_id,
    d.deck_name,
    COUNT(b.borrow_code) AS total_borrows,
    AVG(DATEDIFF(b.return_date, b.request_date)) AS avg_duration
FROM deck d
LEFT JOIN borrow_request b ON d.deck_id = b.deck_id
GROUP BY d.deck_id
ORDER BY total_borrows DESC;