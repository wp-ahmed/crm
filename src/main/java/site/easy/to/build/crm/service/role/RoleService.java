package site.easy.to.build.crm.service.role;

import site.easy.to.build.crm.entity.Role;

import java.util.List;
import java.util.Optional;

public interface RoleService {
    public List<Role> getAllRoles();
    public Role findByName(String name);
    public Optional<Role> findById(int RoleId);
}
