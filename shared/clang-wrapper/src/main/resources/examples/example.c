// Example C file for testing Clang wrapper
// Part of GRA-2: Basic implementation test

#include <stdio.h>

// Simple function to test parsing
int add(int a, int b) {
    return a + b;
}

// Another function with different types
double multiply(double x, double y) {
    return x * y;
}

// Structure definition
struct Point {
    int x;
    int y;
};

// Function using structure
struct Point create_point(int x, int y) {
    struct Point p;
    p.x = x;
    p.y = y;
    return p;
}

// Main function
int main() {
    int result = add(1, 2);
    double product = multiply(3.0, 4.0);
    struct Point p = create_point(10, 20);
    
    printf("Result: %d\n", result);
    printf("Product: %f\n", product);
    printf("Point: (%d, %d)\n", p.x, p.y);
    
    return 0;
}
