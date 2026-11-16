-- Initialize fresh database for Speed Typing Online desktop app
CREATE DATABASE IF NOT EXISTS speedtyping_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE speedtyping_db;

-- 1. Users table (password stored as salted PBKDF2 hash)
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) DEFAULT '',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 2. Typing texts / prompts library
CREATE TABLE IF NOT EXISTS typing_texts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    difficulty VARCHAR(20) DEFAULT 'Medium',
    content TEXT NOT NULL
) ENGINE=InnoDB;

-- 3. Typing results / history table
CREATE TABLE IF NOT EXISTS typing_results (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    wpm INT NOT NULL,
    raw_wpm INT NOT NULL,
    accuracy DECIMAL(5,2) NOT NULL,
    correct_chars INT NOT NULL,
    incorrect_chars INT NOT NULL,
    total_chars INT NOT NULL,
    duration_seconds INT NOT NULL,
    text_title VARCHAR(100) DEFAULT 'Standard Test',
    test_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_results FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Pre-populate typing_texts with realistic speedtyping passages
INSERT INTO typing_texts (title, difficulty, content) VALUES
('The Quick Brown Fox', 'Easy',
'The quick brown fox jumps over the lazy dog. Pack my box with five dozen liquor jugs. How vexingly quick daft zebras jump! Sphinx of black quartz, judge my vow. Two driven jocks help fax my big quiz.'),

('The Journey of Learning', 'Easy',
'Practice makes progress. Every master was once a beginner who refused to quit. When you focus on accuracy first, speed naturally follows. Keep your fingers curved gently over the home row and maintain a steady rhythm as you type.'),

('Nature and Mountains', 'Medium',
'The morning sun began to pierce through the thick canopy of ancient pine trees, casting long golden shadows across the forest floor. A gentle breeze carried the crisp fragrance of cedar and damp earth. Far in the distance, snow-capped peaks stood majestic against a clear sapphire sky.'),

('The Art of Programming', 'Medium',
'Programming is the modern craft of thinking with clarity and precision. A software engineer shapes complex ideas into elegant logic, creating systems that connect people, automate arduous tasks, and open new horizons of innovation and creativity across the globe.'),

('The Great Gatsby Excerpt', 'Hard',
'In my younger and more vulnerable years my father gave me some advice that I have been turning over in my mind ever since. Whenever you feel like criticizing anyone, he told me, just remember that all the people in this world haven''t had the advantages that you have had.'),

('Technology and Innovation', 'Hard',
'The rapid evolution of artificial intelligence and distributed computing is fundamentally reshaping how human civilization communicates, collaborates, and discovers new knowledge. Breakthroughs in quantum hardware, cryptographic protocols, and neural architectures continue to redefine the boundaries of what was once considered impossible.');
