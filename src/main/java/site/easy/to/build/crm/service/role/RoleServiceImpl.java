package site.easy.to.build.crm.service.role;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import site.easy.to.build.crm.entity.Role;
import site.easy.to.build.crm.repository.RoleRepository;

import java.util.List;
import java.util.Optional;

@Service
public class RoleServiceImpl implements RoleService{

    private final RoleRepository roleRepository;

    @Autowired
    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @Override
    public Role findByName(String name) {
        return roleRepository.findByName(name);
    }

    @Override
    public Optional<Role> findById(int RoleId) {
        return roleRepository.findById(RoleId);
    }

}
