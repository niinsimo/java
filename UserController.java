package ee.coop.core.controller;

import com.google.zxing.WriterException;
import com.itextpdf.text.DocumentException;
import ee.coop.core.domain.User;
import ee.coop.core.dto.UserData;
import ee.coop.core.service.UserService;
import ee.coop.delivery.domain.Cabinet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/core/users")
public class UsersController {

    @Autowired
    private UserService userService;

    private static final String qrCodeFilename = "qrcode.pdf";

    @GetMapping
    public List<User> getUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @GetMapping("{id}/profile")
    public UserData getUserDataById(@PathVariable Long id) {
        return userService.getUserDataByid(id);
    }

    @PostMapping
    public void createUser(@RequestBody User user) throws Exception {
        userService.createUser(user);
    }

    @PostMapping("{id}/cabinet")
    public void saveUserCabinet(@PathVariable Long id, @RequestBody Cabinet cabinet) throws Exception {
        userService.saveUserCabinet(cabinet.getId(), id);
    }

    @DeleteMapping("{id}/{cabinetId}")
    public void deleteUserCabinet(@PathVariable Long id, @PathVariable Long cabinetId) throws Exception {
        userService.deleteUserCabinet(cabinetId, id);
    }

    @PutMapping("/edit/{id}")
    public void editUser(
            @PathVariable("id") Long id,
            @RequestBody User editedUser) throws Exception {
        userService.editUser(id, editedUser);
    }

    @DeleteMapping("delete/{id}")
    public ResponseEntity delete(@PathVariable("id") Long id) {
        userService.deleteUser(id);
        return new ResponseEntity(HttpStatus.OK);
    }
}
