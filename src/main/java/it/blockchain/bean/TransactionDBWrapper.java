
package it.blockchain.bean;

import java.io.Serializable;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

public class TransactionDBWrapper implements Serializable
{

    @SerializedName("transactionHash")
    @Expose
    private String transactionHash;
    @SerializedName("blockHash")
    @Expose
    private String blockHash;
    @SerializedName("transactionInputs")
    @Expose
    private List<String> transactionInputs = null;
    @SerializedName("transactionDBOutputs")
    @Expose
    private List<TransactionDBOutput> transactionDBOutputs = null;
    private final static long serialVersionUID = -7794855747985696816L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public TransactionDBWrapper() {
    }

    /**
     * 
     * @param transactionHash
     * @param transactionDBOutputs
     * @param transactionInputs
     * @param blockHash
     */
    public TransactionDBWrapper(String transactionHash, String blockHash, List<String> transactionInputs, List<TransactionDBOutput> transactionDBOutputs) {
        super();
        this.transactionHash = transactionHash;
        this.blockHash = blockHash;
        this.transactionInputs = transactionInputs;
        this.transactionDBOutputs = transactionDBOutputs;
    }

    public String getTransactionHash() {
        return transactionHash;
    }

    public void setTransactionHash(String transactionHash) {
        this.transactionHash = transactionHash;
    }

    public String getBlockHash() {
        return blockHash;
    }

    public void setBlockHash(String blockHash) {
        this.blockHash = blockHash;
    }

    public List<String> getTransactionInputs() {
        return transactionInputs;
    }

    public void setTransactionInputs(List<String> transactionInputs) {
        this.transactionInputs = transactionInputs;
    }

    public List<TransactionDBOutput> getTransactionDBOutputs() {
        return transactionDBOutputs;
    }

    public void setTransactionDBOutputs(List<TransactionDBOutput> transactionDBOutputs) {
        this.transactionDBOutputs = transactionDBOutputs;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("transactionHash", transactionHash).append("blockHash", blockHash).append("transactionInputs", transactionInputs).append("transactionDBOutputs", transactionDBOutputs).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(transactionHash).append(transactionDBOutputs).append(transactionInputs).append(blockHash).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof TransactionDBWrapper) == false) {
            return false;
        }
        TransactionDBWrapper rhs = ((TransactionDBWrapper) other);
        return new EqualsBuilder().append(transactionHash, rhs.transactionHash).append(transactionDBOutputs, rhs.transactionDBOutputs).append(transactionInputs, rhs.transactionInputs).append(blockHash, rhs.blockHash).isEquals();
    }

}
