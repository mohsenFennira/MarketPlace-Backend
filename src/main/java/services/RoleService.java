package services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repositories.RoleRepository;

@Service
public class RoleService implements RoleInterface {

    @Autowired
    RoleRepository roleRepository;
}
