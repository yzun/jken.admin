/*
 * Copyright (c) 2020.
 * @Link: http://jken.site
 * @Author: ken kong
 * @LastModified: 2020-02-03T21:22:42.936+08:00
 */

package jken.module.core.controller;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.querydsl.core.types.Predicate;
import jken.integration.Authority;
import jken.integration.AuthorityUtils;
import jken.integration.IntegrationService;
import jken.module.core.entity.MenuItem;
import jken.module.core.entity.Role;
import jken.module.core.service.MenuItemService;
import jken.support.data.TreeHelper;
import jken.support.mvc.DataWrap;
import jken.support.web.CrudController;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/role")
public class RoleController extends CrudController<Role, Long> {

    @Autowired
    private MenuItemService menuItemService;

    @Override
    public Page<Role> list(Predicate predicate, @PageableDefault(sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return super.doInternalPage(predicate, pageable);
    }

    @GetMapping(value = "/{id}/user", produces = "text/html")
    public String showUsers(@PathVariable("id") Role entity, Model model) {
        model.addAttribute("entity", entity);
        return getViewDir() + "/user";
    }

    @GetMapping(value = "/{id}/authority", produces = "text/html")
    public String showAuthorities(@PathVariable("id") Role entity, Model model) {
        model.addAttribute("entity", entity);

        return getViewDir() + "/authority";
    }

    @GetMapping(value = "/{id}/authority", produces = "application/json")
    @ResponseBody
    public DataWrap getAuthorities(@PathVariable("id") Role entity, @QuerydslPredicate(root = MenuItem.class) Predicate predicate) {
        Iterable<MenuItem> roots = menuItemService.findAll(predicate);
        List<MenuItem> menus = TreeHelper.toTree(Lists.newArrayList(roots));
        Set<Authority> authorities = IntegrationService.getAuthorities();
        return DataWrap.of(convertToAuth(menus, entity.getMenuItems(), authorities, entity.getAuthorities()));
    }

    @PostMapping(value = "/{id}/authority")
    @ResponseBody
    public void updateAuthorities(@PathVariable("id") Role entity, @RequestBody(required = false) MultiValueMap<String, String> items) {
        if (items == null || items.size() == 0) {
            entity.setMenuItems(null);
            entity.setAuthorities(null);
        } else {
            Set<String> set = Sets.newHashSet(items.toSingleValueMap().values());

            Set<Long> menuIds = Sets.filter(set, item -> StringUtils.startsWith(item, "menu-")).stream().map(item -> Long.valueOf(StringUtils.removeStart(item, "menu-"))).collect(Collectors.toSet());
            entity.setMenuItems(menuItemService.findAllById(menuIds));

            List<String> authorities = Sets.filter(set, item -> StringUtils.startsWith(item, "authority-")).stream().map(item -> StringUtils.removeStart(item, "authority-")).collect(Collectors.toList());
            entity.setAuthorities(authorities);
        }
        getService().save(entity);
    }

    private List<Map<String, Object>> convertToAuth(final List<MenuItem> menus, final List<MenuItem> selectedMenuItems, final Set<Authority> authorities, final List<String> selectedAuthorities) {
        if (menus == null || menus.size() == 0) {
            return null;
        }
        return menus.stream().map(item -> {
            Map<String, Object> data = new HashMap<>();
            data.put("value", "menu-" + item.getId());
            data.put("name", item.getName());
            data.put("checked", selectedMenuItems != null && selectedMenuItems.contains(item));
            data.put("disabled", false);

            List<Map<String, Object>> list = convertToAuth(item.getChildren(), selectedMenuItems, authorities, selectedAuthorities);

            String authString = item.getAuthorities();
            if (!Strings.isNullOrEmpty(authString)) {
                String[] auths = authString.split(",");
                if (list == null) {
                    list = new ArrayList<>();
                }
                for (String auth : auths) {
                    Authority authority = AuthorityUtils.findByCode(authorities, auth);
                    if (authority != null) {
                        Map<String, Object> authMap = new HashMap<>();
                        authMap.put("value", "authority-" + authority.getCode());
                        authMap.put("name", authority.getName());
                        authMap.put("checked", selectedAuthorities != null && selectedAuthorities.contains(authority.getCode()));
                        authMap.put("disabled", false);
                        authMap.put("auth", true);
                        list.add(authMap);
                    }
                }
            }
            if (list != null && list.size() > 0) {
                data.put("list", list);
            }
            return data;
        }).collect(Collectors.toList());
    }
}
