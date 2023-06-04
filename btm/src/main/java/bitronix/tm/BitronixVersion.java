/* This is generated code, do not modify */
package bitronix.tm;

/**
 * get version attribute from MANIFEST.MF
 *
 * @author laingke
 */
public final class BitronixVersion {
    private BitronixVersion() {
        throw new IllegalStateException("Bitronix version utility class, unable to be instantiated");
    }

    public static String getVersion() {
        Package pkg = BitronixVersion.class.getPackage();
        return (pkg != null ? pkg.getImplementationVersion() : null);
    }
}