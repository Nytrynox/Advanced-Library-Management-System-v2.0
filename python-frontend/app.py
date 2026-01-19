from flask import Flask, render_template, request, redirect, url_for, flash, jsonify
from flask_cors import CORS
import requests
import os
from datetime import datetime, timedelta
from dotenv import load_dotenv
from analytics import analytics_bp

# Load environment variables
load_dotenv()

app = Flask(__name__)
CORS(app)
app.secret_key = os.getenv('SECRET_KEY', 'your-secret-key')

# Register blueprints
app.register_blueprint(analytics_bp, url_prefix='/analytics')

# Backend API URL
API_URL = os.getenv('API_URL', 'http://localhost:8080/api')

@app.route('/')
def home():
    # Get ONLY real stats from analytics API - NO FALLBACK DATA
    stats = {
        'total_books': 0,
        'available_books': 0,
        'total_borrows': 0,
        'average_rating': 0
    }
    
    try:
        # Use real analytics API endpoint instead of calculating from books list
        stats_response = requests.get(f"{API_URL}/analytics/stats", timeout=5)
        if stats_response.status_code == 200:
            real_stats = stats_response.json()
            stats = {
                'total_books': real_stats.get('totalBooks', 0),
                'available_books': real_stats.get('availableBooks', 0),
                'total_borrows': real_stats.get('totalBorrows', 0),
                'average_rating': real_stats.get('averageRating', 0)
            }
            print(f"✓ Home page using REAL stats from DB: {stats}")
        else:
            print(f"✗ Cannot fetch real stats for home page: {stats_response.status_code}")
    except Exception as e:
        print(f"✗ Error fetching real stats for home: {e}")
        # Keep stats as 0 - NO FAKE DATA
    
    return render_template('index-clean.html', stats=stats)

@app.route('/books')
def books():
    response = requests.get(f"{API_URL}/books")
    books = response.json()
    return render_template('books-clean.html', books=books)

@app.route('/books/search')
def search_books():
    query = request.args.get('query', '')
    response = requests.get(f"{API_URL}/books/search", params={'title': query})
    books = response.json()
    return render_template('books.html', books=books, query=query)

@app.route('/books/add', methods=['GET', 'POST'])
def add_book():
    if request.method == 'POST':
        try:
            book_data = request.get_json()
            
            # Clean and validate the data
            book_data = {
                'title': book_data.get('title').strip(),
                'author': book_data.get('author').strip(),
                'isbn': book_data.get('isbn').strip(),
                'category': book_data.get('category'),
                'quantity': int(book_data.get('quantity')),
                'available': True,
                'description': book_data.get('description', '').strip(),
                'location': book_data.get('location').strip(),
                'tags': book_data.get('tags', [])
            }
            
            # Validate required fields
            required_fields = ['title', 'author', 'isbn', 'category', 'quantity', 'location']
            for field in required_fields:
                if not book_data.get(field):
                    return jsonify({'success': False, 'message': f'{field.capitalize()} is required'}), 400
            
            # Send request to backend API
            response = requests.post(f"{API_URL}/books", json=book_data)
            
            if response.status_code == 200:
                return jsonify({
                    'success': True,
                    'message': 'Book added successfully!',
                    'data': response.json()
                })
            else:
                error_message = response.json().get('message', 'Failed to add book')
                return jsonify({'success': False, 'message': error_message}), response.status_code
                
        except Exception as e:
            app.logger.error(f"Error adding book: {str(e)}")
            return jsonify({'success': False, 'message': str(e)}), 500
            
    return render_template('add_book.html')

@app.route('/api/books/<book_id>/borrow', methods=['POST'])
def borrow_book(book_id):
    try:
        # Forward the request to Java backend
        response = requests.post(f"{API_URL}/books/{book_id}/borrow")
        
        if response.status_code == 200:
            return jsonify({
                'success': True,
                'message': 'Book borrowed successfully!',
                'data': response.json()
            })
        elif response.status_code == 400:
            return jsonify({
                'success': False,
                'message': 'Book is not available for borrowing'
            }), 400
        elif response.status_code == 404:
            return jsonify({
                'success': False,
                'message': 'Book not found'
            }), 404
        else:
            return jsonify({
                'success': False,
                'message': 'Failed to borrow book'
            }), response.status_code
            
    except Exception as e:
        app.logger.error(f"Error borrowing book: {str(e)}")
        return jsonify({
            'success': False,
            'message': 'Server error occurred'
        }), 500

@app.route('/api/books/<book_id>/return', methods=['POST'])
def return_book(book_id):
    try:
        # Forward the request to Java backend
        response = requests.post(f"{API_URL}/books/{book_id}/return")
        
        if response.status_code == 200:
            return jsonify({
                'success': True,
                'message': 'Book returned successfully!',
                'data': response.json()
            })
        elif response.status_code == 400:
            return jsonify({
                'success': False,
                'message': 'Book is not currently borrowed'
            }), 400
        elif response.status_code == 404:
            return jsonify({
                'success': False,
                'message': 'Book not found'
            }), 404
        else:
            return jsonify({
                'success': False,
                'message': 'Failed to return book'
            }), response.status_code
            
    except Exception as e:
        app.logger.error(f"Error returning book: {str(e)}")
        return jsonify({
            'success': False,
            'message': 'Server error occurred'
        }), 500

@app.route('/login', methods=['GET', 'POST'])
def login():
    if request.method == 'POST':
        user_data = {
            'username': request.form['username'],
            'password': request.form['password']
        }
        response = requests.post(f"{API_URL}/auth/login", json=user_data)
        if response.status_code == 200:
            # Handle successful login
            flash('Logged in successfully!', 'success')
            return redirect(url_for('home'))
        flash('Invalid credentials!', 'error')
    return render_template('login.html')

# Advanced Features Routes
@app.route('/recommendations')
def recommendations():
    try:
        response = requests.get(f"{API_URL}/books/recommendations")
        books = response.json()
        return render_template('recommendations.html', books=books)
    except:
        # If API fails, return empty list
        return render_template('recommendations.html', books=[])

@app.route('/popular')
def popular_books():
    try:
        response = requests.get(f"{API_URL}/books/popular")
        books = response.json()
        return render_template('popular.html', books=books)
    except:
        return render_template('popular.html', books=[])

@app.route('/categories')
def browse_categories():
    try:
        response = requests.get(f"{API_URL}/books/categories")
        categories = response.json()
        return render_template('categories.html', categories=categories)
    except:
        return render_template('categories.html', categories=[])

@app.route('/advanced-search')
def advanced_search():
    title = request.args.get('title', '')
    author = request.args.get('author', '')
    category = request.args.get('category', '')
    isbn = request.args.get('isbn', '')
    availability = request.args.get('availability', '')
    
    if any([title, author, category, isbn, availability]):
        try:
            params = {k: v for k, v in request.args.items() if v}
            response = requests.get(f"{API_URL}/books/search/advanced", params=params)
            books = response.json()
        except:
            books = []
    else:
        books = []
    
    return render_template('advanced-search.html', books=books)

@app.route('/analytics')
def analytics_dashboard():
    # STRICT REAL DATA ONLY - NO FALLBACKS OR FAKE DATA
    print("Fetching ONLY real analytics data from MongoDB via Java backend...")
    
    # Initialize with empty values - will only populate with real data
    total_books = 0
    available_books = 0
    total_borrows = 0
    average_rating = 0.0
    categories = []
    category_counts = []
    popular_books_titles = []
    popular_books_counts = []
    popular_books = []
    
    try:
        # Get basic statistics from analytics API - REAL DATA ONLY
        stats_response = requests.get(f"{API_URL}/analytics/stats", timeout=10)
        if stats_response.status_code == 200:
            stats = stats_response.json()
            total_books = stats.get('totalBooks', 0)
            available_books = stats.get('availableBooks', 0)
            total_borrows = stats.get('totalBorrows', 0)
            average_rating = stats.get('averageRating', 0.0)
            print(f"✓ REAL Stats from DB: {stats}")
        else:
            print(f"✗ Failed to fetch real stats: {stats_response.status_code}")
        
        # Get category distribution - REAL DATA ONLY
        categories_response = requests.get(f"{API_URL}/analytics/categories", timeout=10)
        if categories_response.status_code == 200:
            category_data = categories_response.json()
            if category_data:
                categories = list(category_data.keys())
                category_counts = list(category_data.values())
                print(f"✓ REAL Categories from DB: {len(categories)} categories")
            else:
                print("✗ No category data in database")
        else:
            print(f"✗ Failed to fetch real categories: {categories_response.status_code}")
        
        # Get popular books - REAL DATA ONLY
        popular_response = requests.get(f"{API_URL}/analytics/popular-books", timeout=10)
        if popular_response.status_code == 200:
            popular_books_data = popular_response.json()
            if popular_books_data:
                popular_books_titles = [book.get('title', '')[:30] for book in popular_books_data]
                popular_books_counts = [book.get('borrowCount', 0) for book in popular_books_data]
                print(f"✓ REAL Popular books from DB: {len(popular_books_data)} books")
            else:
                print("✗ No popular books data in database")
        else:
            print(f"✗ Failed to fetch real popular books: {popular_response.status_code}")
        
        # Get all books for the table - REAL DATA ONLY
        books_response = requests.get(f"{API_URL}/books", timeout=10)
        if books_response.status_code == 200:
            books = books_response.json()
            if books:
                # Sort books by borrow count for the table - REAL DATA ONLY
                popular_books = sorted(books, key=lambda x: x.get('borrowCount', 0), reverse=True)[:10]
                print(f"✓ REAL Books for table from DB: {len(popular_books)} books")
            else:
                print("✗ No books data in database")
        else:
            print(f"✗ Failed to fetch real books: {books_response.status_code}")
        
        print(f"✓ FINAL REAL DATA: books={total_books}, available={available_books}, borrows={total_borrows}, rating={average_rating}")
            
    except Exception as e:
        print(f"✗ CRITICAL ERROR - Cannot fetch real data: {e}")
        # NO FALLBACKS - Keep everything as 0/empty if cannot connect to real database
    
    return render_template('analytics-clean.html',
                           total_books=total_books,
                           available_books=available_books,
                           total_borrows=total_borrows,
                           average_rating=average_rating,
                           categories={cat: count for cat, count in zip(categories, category_counts)},
                           category_labels=categories,
                           category_data=category_counts,
                           popular_labels=popular_books_titles,
                           popular_data=popular_books_counts,
                           popular_books=popular_books)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5001, debug=True)
