# Real Estate Management System

A comprehensive desktop application for managing real estate listings built with Java Swing. The project demonstrates the practical implementation of fundamental data structures and algorithms in a real-world GUI application.

## Overview

This application allows users to add, update, delete, search, and sort real estate properties efficiently. It features multiple data structures working together to provide different functionalities such as undo operations, request queuing, and ordered listings.

## Features

### Core Operations
- **Add New Property** — Register new real estate listings with detailed information
- **Update Property** — Modify existing property details
- **Delete Property** — Remove listings with undo support
- **Search**:
  - Linear search by keyword (title or location)
  - Binary search by ID (demonstrates sorting + binary search)

### Data Structures & Algorithms
- **Linked List** — Main storage for properties
- **Stack** — Undo functionality (restore deleted properties)
- **Queue** — Visit request management (FIFO)
- **Binary Search Tree (BST)** — Ordered traversal by price
- **Quick Sort** — Efficient sorting by multiple criteria
- **Binary Search** — Fast ID-based lookup

### Sorting Capabilities
- Sort by Price
- Sort by Square Meters
- Sort by Number of Rooms
- Default ID-based ordering

### Additional Features
- Visit request queue management
- BST-based sorted display by price
- Responsive table view
- Input validation and error handling

## Technologies Used

- **Java** (JDK 8+)
- **Swing** — GUI framework
- **Custom Data Structures** (no external libraries)

## Project Structure

```
DSA_2/
├── src/
│   └── emlak/
│       ├── Emlak.java
│       ├── Dugum.java
│       ├── BagliListe.java
│       ├── Yigin.java
│       ├── Kuyruk.java
│       ├── AgacDugum.java
│       ├── IkiliAramaAgaci.java
│       └── EmlakUygulamasi.java
├── .gitignore
├── LICENSE
└── README.md
```

## How to Run

### Prerequisites
- Java Development Kit (JDK 8 or higher)
- Any Java IDE (IntelliJ IDEA, Eclipse, NetBeans) or command line

### Running the Application

1. Clone or download the project
2. Compile all Java files:
   ```bash
   javac *.java
   ```
3. Run the main class:
   ```bash
   java EmlakUygulamasi
   ```

Alternatively, open the project in your preferred IDE and run the `EmlakUygulamasi` class.

## Usage Guide

### Managing Properties
- Fill in the form fields (Title, Location, Price, m², Rooms) and click **Ekle**.
- Select a row in the table to **Update** or **Delete**.
- Use **Geri Al (Yığın)** to restore the most recently deleted property.

### Searching & Sorting
- **Doğrusal Arama**: Search by keyword in title or location.
- **İkili Arama (ID)**: Enter ID in the search field for fast lookup.
- Use sorting buttons to reorder the entire list.

### Request Management
- Select a property and click **Ziyaret Talebi Ekle (Kuyruk)**.
- Process pending requests with **Talebi İşle**.

### BST Display
- Click **BST Sıralı Göster** to view properties sorted by price using in-order traversal of the Binary Search Tree.

## Educational Purpose

This project is designed to demonstrate:
- Implementation of Linked List, Stack, and Queue from scratch
- Binary Search Tree operations
- Sorting algorithms (Quick Sort)
- Search algorithms (Linear + Binary)
- Integration of data structures in a GUI application
- Proper memory management and object-oriented design
