package city.windmill.ingameime.client.jni;
@FunctionalInterface
public interface ICommitListener {
    String onCommit(String commit);
}