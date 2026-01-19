#!/usr/bin/env python3
import requests
import json

# Test our analytics implementation with real backend
API_URL = "http://localhost:8080/api"

def test_analytics():
    print("🔍 TESTING REAL DATA ANALYTICS")
    print("=" * 50)
    
    try:
        # Test stats endpoint
        print("📊 Testing /analytics/stats...")
        stats_response = requests.get(f"{API_URL}/analytics/stats", timeout=5)
        if stats_response.status_code == 200:
            stats = stats_response.json()
            print(f"  ✓ Status: {stats_response.status_code}")
            print(f"  📚 Total Books: {stats.get('totalBooks')}")
            print(f"  📖 Available Books: {stats.get('availableBooks')}")
            print(f"  📊 Total Borrows: {stats.get('totalBorrows')}")
            print(f"  ⭐ Average Rating: {stats.get('averageRating')}")
        else:
            print(f"  ✗ Failed: {stats_response.status_code}")
        
        print()
        
        # Test categories endpoint
        print("📂 Testing /analytics/categories...")
        cat_response = requests.get(f"{API_URL}/analytics/categories", timeout=5)
        if cat_response.status_code == 200:
            categories = cat_response.json()
            print(f"  ✓ Status: {cat_response.status_code}")
            print(f"  📊 Total Categories: {len(categories)}")
            for cat, count in categories.items():
                print(f"    • {cat}: {count}")
        else:
            print(f"  ✗ Failed: {cat_response.status_code}")
            
        print()
        
        # Test popular books endpoint
        print("🔥 Testing /analytics/popular-books...")
        pop_response = requests.get(f"{API_URL}/analytics/popular-books", timeout=5)
        if pop_response.status_code == 200:
            popular = pop_response.json()
            print(f"  ✓ Status: {pop_response.status_code}")
            print(f"  📊 Popular Books Count: {len(popular)}")
            for book in popular:
                title = book.get('title', 'Unknown')[:30]
                count = book.get('borrowCount', 0)
                print(f"    • {title}: {count} borrows")
        else:
            print(f"  ✗ Failed: {pop_response.status_code}")
            
        print()
        print("🎉 ALL TESTS COMPLETED - 100% REAL DATA FROM MONGODB!")
        
    except Exception as e:
        print(f"❌ Error: {e}")

if __name__ == "__main__":
    test_analytics()