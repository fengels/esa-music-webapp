package de.esa.user.auth.jpa;

import de.esa.user.auth.domain.IllegalNameException;
import de.esa.user.auth.domain.UserAuth;
import de.esa.user.auth.domain.UserObject;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateless(name="ejb/UserAuth",mappedName="UserAuth")
@Remote(UserAuth.class)
public class UserAuthImpl implements UserAuth {
    
   @PersistenceContext
    private EntityManager em;

    public UserAuthImpl() {
        Logger.getLogger(UserAuthImpl.class.getName()).log(Level.INFO, "UserAuthImpl created..");
    }
    
    @Override
    public UserObject login(String name, String password) throws IllegalNameException {
        Logger.getLogger(UserAuthImpl.class.getName()).log(Level.INFO, "login: Username: " + name + " Password: " + password);
      /*  if (name == null || password == null || name.equals("") || password.equals("")) {
            throw new IllegalNameException("Enter a valid username and password.");
        }*/

        final UserObject user = getUserByName(name);

        if (user == null) {
            return null;
        }

        if (user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    @Override
    public UserObject register(String name, String password) throws IllegalNameException {
        Logger.getLogger(UserAuthImpl.class.getName()).log(Level.INFO, "Username: " + name + " Password: " + password);
        
       /* if (name == null || password == null || name.equals("") || password.equals("")) {
            final String errMsg = "Enter a valid username and password.";
            Logger.getLogger(UserAuthImpl.class.getName()).log(Level.INFO, errMsg);
            throw new IllegalNameException(errMsg);
        }

        final UserObject user = getUserByName(name);
        if (user != null) {
            final String errMsg = "The username is already taken.";
            Logger.getLogger(UserAuthImpl.class.getName()).log(Level.INFO, errMsg);
            throw new IllegalNameException(errMsg);
        }*/
        em.getTransaction().begin();
        em.persist(new UserObject(name, password));
        em.getTransaction().commit();

        return login(name, password);
    }

    @Override
    public boolean deleteUser(final UserObject user) {
        if (user == null) {
            throw new IllegalArgumentException("user-UserObject-Object is NULL.");
        }

        final String username = user.getName();

        if (user.getId() < 0) {
            throw new IllegalArgumentException("user-Object has an illegal ID (<0)");
        }


        final UserObject userToDelete = em.find(UserObject.class, user.getId());

        em.getTransaction().begin();
        em.remove(userToDelete);
        em.getTransaction().commit();

        if (getUserByName(username) == null) {
            return false;
        }
        em.close();

        return true;
    }

    @Override
    public UserObject edit(final UserObject user) {
        return null;
    }

    public UserObject getUserByName(final String name) {
        Logger.getLogger(UserAuthImpl.class.getName()).log(Level.INFO, "Username: " + name);
        final List<UserObject> resultList = em.createQuery("Select a From UserObject a", UserObject.class).getResultList();
        //		JPAUtil.shutdown();

        for (UserObject next : resultList) {
            if (next.getName().equals(name)) {
                return next;
            }
        }
        return null;
    }
}