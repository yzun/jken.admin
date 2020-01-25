package jken.site.modules.core.controller;

import com.querydsl.core.types.Predicate;
import jken.site.modules.core.entity.Role;
import jken.site.support.web.CrudController;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/role")
public class RoleController extends CrudController<Role, Long> {
    @Override
    public Page<Role> list(Predicate predicate, Pageable pageable) {
        return super.doInternalPage(predicate, pageable);
    }
}
