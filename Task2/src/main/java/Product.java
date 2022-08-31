public class Product {
    private String name;
    private int mileage;

    public Product(String name, int mileage) {
        this.name = name;
        this.mileage = mileage;
    }

    public String getName() {
        return name;
    }

    public int getMileage() {
        return mileage;
    }

    @Override
    public int hashCode() {
        return mileage;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Product) {
            Product product = (Product) obj;
            return (product.name.equals(this.name) && product.mileage == this.mileage);
        }
        return false;
    }
}