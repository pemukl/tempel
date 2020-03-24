public enum Role {
    ABENTEURER,
    WAECHTERIN;

    @Override
    public String toString() {
        if (this == ABENTEURER)
            return "Abenteurer";
        else
            return "Wächterin";
    }
}