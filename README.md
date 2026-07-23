# Real Estate Listing Application

A comprehensive desktop application for managing real estate listings built with **Java Swing**. The project demonstrates the practical implementation of fundamental data structures and algorithms in a modern, user-friendly GUI application with separate **Admin** and **User** modes.

## Overview

This application allows users to efficiently manage real estate properties with full CRUD operations, advanced search and sorting capabilities, favorites system, visit request management, and detailed statistics. It features multiple custom data structures working together seamlessly.

### Features

### Core Operations
- **Add / Update / Delete** properties with detailed information (title, type, listing type, price, m², rooms, location, image)
- **Favorites System** — Users can add/remove properties to their favorites
- **Undo Delete** using Stack (restore last deleted property)
- **Persistent Storage** — All data is saved to and loaded from files

### Search & Sorting
- **Linear Search** by keyword (title or location)
- **Binary Search** by ID (after sorting)
- **Quick Sort** support for multiple criteria:
  - By Price
  - By Square Meters
  - By Number of Rooms
  - By ID (default)

### Data Structures Used
- **Linked List** (`BagliListe`) — Main storage for properties
- **Stack** (`Yigin`) — Undo functionality for deletions
- **Queue** (`Kuyruk`) — Visit request management (FIFO)
- **Binary Search Tree** (`IkiliAramaAgaci`) — Ordered display by price
- **Custom Node** implementations

### User Interface & Experience
- Modern, responsive design with custom color scheme
- **Left Sidebar Menu** with hover effects
- **Statistics Dashboard** (Total Properties, Favorites, Total Views, Average Price, etc.)
- Separate **Admin Panel** and **User Panel**
- Image support for properties
- Visit request system with approval workflow

### Additional Features
- Visit request queue management (add, process, approve)
- In-order BST traversal for price-sorted display
- Real-time statistics updating
- Input validation and error handling
- Multi-user simulation (Admin / Customer)

## Technologies Used

- **Java** (JDK 8+)
- **Swing** — GUI framework
- **Custom Data Structures** (no external libraries)
- File I/O for persistence

## Project Structure

```bash
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
├── emlak_verileri.txt
├── favoriler.txt
├── kullanicilar.txt
├── ziyaret_talepleri.txt
├── .gitignore
├── LICENSE
└── README.md
```

## How to Run

### Prerequisites
- Java Development Kit (JDK 8 or higher)

### Running the Application

1. Clone the repository:
   ```bash
   git clone https://github.com/428937/DSA_2.git
   ```

2. Open the project in your IDE (recommended: IntelliJ IDEA)

3. Run the main class `EmlakUygulamasi`

   Or from command line:
   ```bash
   cd DSA_2/src/emlak
   javac *.java
   java EmlakUygulamasi
   ```

You can start as **Admin** or **User** by modifying the constructor call in `main` method.

## Usage Guide

### For Administrators
- Full access to add, update, and delete properties
- Manage visit requests
- View all statistics

### For Users
- Browse properties
- Add to favorites
- Create visit requests
- Search and sort listings

### Key Buttons
- **Ekle** → Add new property
- **Güncelle** → Update selected property
- **Sil** → Delete with undo option
- **Favorilere Ekle** → Add to favorites
- **Ziyaret Talebi Ekle** → Add to request queue
- **BST Sıralı Göster** → Show properties sorted by price using BST
- **Geri Al** → Restore last deleted item

## Educational Purpose

This project is designed to demonstrate:
- Implementation of all major linear and non-linear data structures from scratch
- Integration of data structures in a real-world GUI application
- Algorithm implementation (Quick Sort, Binary Search, BST operations)
- Clean Object-Oriented Design principles
- File-based persistence
- Modern UI/UX practices with Swing

