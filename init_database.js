// Initialize Library Management System Database
// This script creates collections and populates sample data

const db = connect('mongodb://localhost:27017/library_management');

// Drop existing collections if they exist
db.books.drop();
db.users.drop();
db.categories.drop();
db.loans.drop();

// Create Books Collection with sample data
db.books.insertMany([
    {
        title: "Artificial Intelligence: A Modern Approach",
        author: "Stuart Russell",
        isbn: "978-0134610993",
        category: "Computer Science",
        publisher: "Pearson",
        year: 2020,
        copies: 5,
        available: 5,
        description: "Comprehensive introduction to AI",
        tags: ["AI", "Machine Learning", "Computer Science"]
    },
    {
        title: "Clean Code",
        author: "Robert Martin",
        isbn: "978-0132350884",
        category: "Software Engineering",
        publisher: "Prentice Hall",
        year: 2008,
        copies: 3,
        available: 3,
        description: "A handbook of agile software craftsmanship",
        tags: ["Programming", "Best Practices", "Software Engineering"]
    },
    {
        title: "Design Patterns",
        author: "Gang of Four",
        isbn: "978-0201633610",
        category: "Software Engineering",
        publisher: "Addison-Wesley",
        year: 1994,
        copies: 4,
        available: 4,
        description: "Elements of reusable object-oriented software",
        tags: ["Design Patterns", "OOP", "Software Architecture"]
    },
    {
        title: "Introduction to Algorithms",
        author: "Thomas Cormen",
        isbn: "978-0262033848",
        category: "Computer Science",
        publisher: "MIT Press",
        year: 2009,
        copies: 6,
        available: 6,
        description: "Comprehensive textbook on algorithms",
        tags: ["Algorithms", "Data Structures", "Computer Science"]
    },
    {
        title: "Python Crash Course",
        author: "Eric Matthes",
        isbn: "978-1593279288",
        category: "Programming",
        publisher: "No Starch Press",
        year: 2019,
        copies: 4,
        available: 4,
        description: "A hands-on, project-based introduction to programming",
        tags: ["Python", "Programming", "Beginners"]
    },
    {
        title: "The Pragmatic Programmer",
        author: "Andrew Hunt",
        isbn: "978-0135957059",
        category: "Software Engineering",
        publisher: "Addison-Wesley",
        year: 2019,
        copies: 3,
        available: 3,
        description: "Your journey to mastery",
        tags: ["Programming", "Career", "Best Practices"]
    },
    {
        title: "Database System Concepts",
        author: "Abraham Silberschatz",
        isbn: "978-0078022159",
        category: "Databases",
        publisher: "McGraw-Hill",
        year: 2019,
        copies: 5,
        available: 5,
        description: "Comprehensive database systems textbook",
        tags: ["Databases", "SQL", "Data Management"]
    },
    {
        title: "Computer Networks",
        author: "Andrew Tanenbaum",
        isbn: "978-0132126953",
        category: "Networking",
        publisher: "Pearson",
        year: 2010,
        copies: 4,
        available: 4,
        description: "Classic textbook on computer networking",
        tags: ["Networking", "Internet", "Protocols"]
    },
    {
        title: "Operating System Concepts",
        author: "Abraham Silberschatz",
        isbn: "978-1118063330",
        category: "Operating Systems",
        publisher: "Wiley",
        year: 2018,
        copies: 5,
        available: 5,
        description: "Fundamentals of operating systems",
        tags: ["OS", "Systems Programming", "Computer Science"]
    },
    {
        title: "Machine Learning Yearning",
        author: "Andrew Ng",
        isbn: "978-0999820907",
        category: "Machine Learning",
        publisher: "DeepLearning.AI",
        year: 2018,
        copies: 4,
        available: 4,
        description: "Technical strategy for AI engineers",
        tags: ["Machine Learning", "AI", "Deep Learning"]
    }
]);

// Create Users Collection
db.users.insertMany([
    {
        username: "admin",
        password: "admin123",
        email: "admin@library.com",
        role: "admin",
        name: "Admin User",
        createdAt: new Date()
    },
    {
        username: "alice",
        password: "alice123",
        email: "alice@example.com",
        role: "member",
        name: "Alice Anderson",
        createdAt: new Date()
    },
    {
        username: "bob",
        password: "bob123",
        email: "bob@example.com",
        role: "member",
        name: "Bob Brown",
        createdAt: new Date()
    },
    {
        username: "charlie",
        password: "charlie123",
        email: "charlie@example.com",
        role: "member",
        name: "Charlie Chen",
        createdAt: new Date()
    },
    {
        username: "diana",
        password: "diana123",
        email: "diana@example.com",
        role: "member",
        name: "Diana Davis",
        createdAt: new Date()
    }
]);

// Create Categories Collection
db.categories.insertMany([
    { name: "Computer Science", description: "Books on computer science topics" },
    { name: "Software Engineering", description: "Software development and engineering" },
    { name: "Programming", description: "Programming languages and techniques" },
    { name: "Databases", description: "Database systems and management" },
    { name: "Networking", description: "Computer networks and communications" },
    { name: "Operating Systems", description: "Operating system concepts" },
    { name: "Machine Learning", description: "Machine learning and AI" },
    { name: "Web Development", description: "Web technologies and frameworks" },
    { name: "Mobile Development", description: "Mobile app development" },
    { name: "Security", description: "Cybersecurity and information security" }
]);

// Create Loans Collection (some sample loans)
db.loans.insertMany([
    {
        userId: "alice",
        bookId: ObjectId(),
        bookTitle: "Python Crash Course",
        loanDate: new Date("2025-11-15"),
        dueDate: new Date("2025-12-15"),
        status: "active"
    },
    {
        userId: "bob",
        bookId: ObjectId(),
        bookTitle: "Clean Code",
        loanDate: new Date("2025-11-10"),
        dueDate: new Date("2025-12-10"),
        status: "active"
    }
]);

// Create indexes for better performance
db.books.createIndex({ title: "text", author: "text", description: "text" });
db.books.createIndex({ category: 1 });
db.books.createIndex({ isbn: 1 }, { unique: true });
db.users.createIndex({ username: 1 }, { unique: true });
db.users.createIndex({ email: 1 }, { unique: true });
db.loans.createIndex({ userId: 1 });
db.loans.createIndex({ status: 1 });

print("✅ Database initialized successfully!");
print("📚 Books inserted: " + db.books.countDocuments());
print("👥 Users inserted: " + db.users.countDocuments());
print("📁 Categories inserted: " + db.categories.countDocuments());
print("📝 Loans inserted: " + db.loans.countDocuments());
