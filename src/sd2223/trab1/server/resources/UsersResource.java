package sd2223.trab1.server.resources;

import sd2223.trab1.api.User;
import sd2223.trab1.api.rest.UsersService;
import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Class UsersResource - handles users' management
 *
 * @author Francisco Parrinha 	58360
 * @author Martin Magdalinchev 	58172
 */
@Singleton
public class UsersResource implements UsersService {

	/** Constants */
	private static final String DOMAIN = "@%s";
	private static final Logger LOG = Logger.getLogger(UsersResource.class.getName());

	/** Variables */
	private final ConcurrentHashMap<String,User> users = new ConcurrentHashMap<>();

	/** Constructor */
	public UsersResource() {
	}

	@Override
	public String createUser(User user) {
		LOG.info("createUser : " + user);
		
		// Check if user data is valid
		if(user.getName() == null || user.getPwd() == null || user.getDisplayName() == null || user.getDomain() == null) {
			LOG.info("User object invalid.");
			throw new WebApplicationException( Status.BAD_REQUEST );
		}
		
		// Insert user, checking if name already exists
		if( users.putIfAbsent(user.getName(), user) != null ) {
			LOG.info("User already exists.");
			throw new WebApplicationException( Status.CONFLICT );
		}

		return user.getName() + String.format(DOMAIN, user.getDomain());
	}
	
	@Override
	public User getUser(String name, String pwd) {
			LOG.info("getUser : user = " + name + "; pwd = " + pwd);
			
			// Check if user is valid
			if(name == null || pwd == null) {
				LOG.info("Name or Password null.");
				throw new WebApplicationException( Status.BAD_REQUEST );
			}

			// Check if user exists
			User user = users.get(name);
			if( user == null ) {
				LOG.info("User does not exist.");
				throw new WebApplicationException( Status.NOT_FOUND );
			}
			
			//Check if the password is correct
			if( !user.getPwd().equals( pwd)) {
				LOG.info("Password is incorrect.");
				throw new WebApplicationException( Status.FORBIDDEN );
			}
			
			return user;
		}

	@Override
	public User updateUser(String name, String pwd, User user) {
		LOG.info("updateUser : user = " + name + "; pwd = " + pwd + " ; user = " + user);

		// Check if user is valid
		if(name == null || pwd == null || !name.equals(user.getName())) {
			LOG.info("Name or Password null or name and new name are different .");
			throw new WebApplicationException( Status.BAD_REQUEST );
		}

		// Check if there is a user
		User _user = users.get(name);
		if (_user == null){
			LOG.info("User does not exist.");
			throw new WebApplicationException(Status.NOT_FOUND);
		}

		// Check if the password is correct
		if (!_user.getPwd().equals(pwd)){
			LOG.info("Password is incorrect.");
			throw new WebApplicationException(Status.FORBIDDEN);
		}

		// update user info
		String newName = user.getDisplayName();
		String newDomain = user.getDomain();
		String newPassword = user.getPwd();

		_user.setDisplayName(newName == null ? _user.getDisplayName() : newName);
		_user.setDomain(newDomain == null ? _user.getDomain() : newDomain);
		_user.setPwd(newPassword == null ? _user.getPwd() : newPassword);
		users.put(name , _user);

		return _user;
	}

	@Override
	public User deleteUser(String name, String pwd) {
		LOG.info("deleteUser : user = " + name + "; pwd = " + pwd);

		// Check if user is valid
		if(name == null || pwd == null) {
			LOG.info("UserId or password null.");
			throw new WebApplicationException( Status.BAD_REQUEST );
		}

		var user = users.get(name);

		// Check if user exists
		if( user == null ) {
			LOG.info("User does not exist.");
			throw new WebApplicationException( Status.NOT_FOUND );
		}

		//Check if the password is correct
		if( !user.getPwd().equals( pwd)) {
			LOG.info("Password is incorrect.");
			throw new WebApplicationException( Status.FORBIDDEN );
		}

		users.remove(user.getName());

		return user;
	}

	@Override
	public List<User> searchUsers(String pattern) {
		LOG.info("searchUsers : pattern = " + pattern);

		if(pattern == null) {
			LOG.info("Pattern is null");
			throw new WebApplicationException( Status.BAD_REQUEST );
		}

		return getPublicUsers(pattern);
	}

	/**
	 * Gets all public users
	 *
	 * @return list of public users
	 */
	private List<User> getPublicUsers(String pattern){
		List<User> result = new LinkedList<>();

		for (User user : users.values()){
			String userName = user.getName().toLowerCase();
			String patt = pattern.toLowerCase();
			if(pattern.equals("") || userName.contains(patt)){
				User newUser = new User(user.getName(), "", user.getDomain(), user.getDisplayName());
				result.add(newUser);
			}
		}

		return result;
	}
}
