package site.easy.to.build.crm.service.customer;

import site.easy.to.build.crm.entity.CustomerLoginInfo;

public interface CustomerLoginInfoService {
    public CustomerLoginInfo findById(int id);

    public CustomerLoginInfo findByEmail(String email);

    public CustomerLoginInfo findByToken(String token);

    public CustomerLoginInfo save(CustomerLoginInfo customerLoginInfo);

    public void delete(CustomerLoginInfo customerLoginInfo);
}
