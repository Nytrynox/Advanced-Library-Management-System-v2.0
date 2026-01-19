from flask import Blueprint, render_template
import requests
from datetime import datetime, timedelta
import json

analytics_bp = Blueprint('analytics', __name__)

@analytics_bp.route('/analytics')
def analytics_dashboard():
    # Get analytics data from Java backend
    try:
        # Get basic statistics
        stats_response = requests.get('http://localhost:8080/api/analytics/stats')
        stats = stats_response.json()

        # Get category distribution
        categories_response = requests.get('http://localhost:8080/api/analytics/categories')
        categories_data = categories_response.json()
        categories = list(categories_data.keys())
        category_counts = list(categories_data.values())

        # Get popular books
        popular_books_response = requests.get('http://localhost:8080/api/analytics/popular-books')
        popular_books = popular_books_response.json()
        popular_books_titles = [book['title'] for book in popular_books]
        popular_books_counts = [book['borrowCount'] for book in popular_books]

        # Get recent activities
        activities_response = requests.get('http://localhost:8080/api/analytics/recent-activities')
        recent_activities = activities_response.json()
    except requests.exceptions.RequestException as e:
        print(f"Error fetching analytics data: {e}")
        # Return error template or default values
        return render_template('analytics.html',
                            analytics={'totalBooks': 0, 'availableBooks': 0, 
                                     'totalBorrows': 0, 'averageRating': 0.0},
                            categories=[],
                            category_counts=[],
                            popular_books_titles=[],
                            popular_books_counts=[],
                            recent_activities=[])
        
        # Process activities for display
        for activity in recent_activities:
            # Convert timestamp to datetime
            activity['date'] = datetime.fromtimestamp(activity['timestamp']/1000).strftime('%Y-%m-%d %H:%M')
            
            # Set status color based on action
            if activity['action'] == 'BORROWED':
                activity['status_color'] = 'warning'
            elif activity['action'] == 'RETURNED':
                activity['status_color'] = 'success'
            elif activity['action'] == 'RESERVED':
                activity['status_color'] = 'info'
            else:
                activity['status_color'] = 'secondary'

        return render_template('analytics.html',
                            analytics=stats,
                            categories=categories,
                            category_counts=category_counts,
                            popular_books_titles=popular_books_titles,
                            popular_books_counts=popular_books_counts,
                            recent_activities=recent_activities)
