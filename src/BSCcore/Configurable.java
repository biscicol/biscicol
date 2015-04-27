
package BSCcore;

/**
 * Interface for objects that can configure themselves using a SettingsManager.
 */
public interface Configurable
{
    /**
     * Tell this object to configure itself using the specified SettingsManager.
     * The SettingsManager should be initialized with an appropriate properties
     * file.
     * 
     * @param sm The SettingsManager this object should use to configure itself.
     */
    public void configure(SettingsManager sm);
}
