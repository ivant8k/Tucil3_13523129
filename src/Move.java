public class Move {
    public char pieceId;
    public String direction; // "kiri", "kanan", "atas", "bawah"
    public int amount;       // jumlah langkah (1 atau lebih)

    public Move(char pieceId, String direction, int amount) {
        this.pieceId = pieceId;
        this.direction = direction;
        this.amount = amount;
    }

    @Override
    public String toString() {
        return pieceId + "-" + direction + (amount > 1 ? "-" + amount : "");
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Move other = (Move) obj;
        return pieceId == other.pieceId && 
               direction.equals(other.direction) && 
               amount == other.amount;
    }
    
    @Override
    public int hashCode() {
        return 31 * (31 * pieceId + direction.hashCode()) + amount;
    }
}