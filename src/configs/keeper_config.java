package configs;
import java.util.*;

public class keeper_config {
	public static final long lockExpireTime = 5000; // if same thread lock one term more than lockExpireTime seconds, keeper release it 
}
