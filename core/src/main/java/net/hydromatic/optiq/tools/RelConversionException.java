package net.hydromatic.optiq.tools;

/**
 * An Exception thrown when attempting conversion to a set of RelNodes.
 */
public class RelConversionException extends Exception{

  public RelConversionException() {
    super();
  }

  public RelConversionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
    
  }

  public RelConversionException(String message, Throwable cause) {
    super(message, cause);
    
  }

  public RelConversionException(String message) {
    super(message);
    
  }

  public RelConversionException(Throwable cause) {
    super(cause);
    
  }
  
}