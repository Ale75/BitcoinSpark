
package it.blockchain.bean;

import java.io.Serializable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

public class TransactionDBOutput implements Serializable
{

    @SerializedName("hash")
    @Expose
    private String hash;
    @SerializedName("value")
    @Expose
    private Double value;
    private final static long serialVersionUID = 1589402349944413989L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public TransactionDBOutput() {
    }

    /**
     * 
     * @param hash
     * @param value
     */
    public TransactionDBOutput(String hash, Double value) {
        super();
        this.hash = hash;
        this.value = value;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("hash", hash).append("value", value).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(hash).append(value).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof TransactionDBOutput) == false) {
            return false;
        }
        TransactionDBOutput rhs = ((TransactionDBOutput) other);
        return new EqualsBuilder().append(hash, rhs.hash).append(value, rhs.value).isEquals();
    }

}
