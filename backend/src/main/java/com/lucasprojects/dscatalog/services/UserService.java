package com.lucasprojects.dscatalog.services;

import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lucasprojects.dscatalog.entities.Role;
import com.lucasprojects.dscatalog.entities.User;
import com.lucasprojects.dscatalog.entities.dtos.UserDTO;
import com.lucasprojects.dscatalog.entities.dtos.UserInsertDTO;
import com.lucasprojects.dscatalog.repositories.RoleRepository;
import com.lucasprojects.dscatalog.repositories.UserRepository;

@Service
public class UserService implements UserDetailsService {

	private static Logger logger = LoggerFactory.getLogger(UserService.class);
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder; 
	
	@Autowired
	private UserRepository repository;
	
	@Autowired
	private RoleRepository roleRepository;

	@Transactional(readOnly = true)
	public Page<UserDTO> findAllPaged(Pageable pageable) {
		Page<User> page = repository.findAll(pageable);

		return page.map(entity -> new UserDTO(entity));
	}

	@Transactional(readOnly = true)
	public UserDTO findById(Long id) {
		Optional<User> obj = repository.findById(id);
		User entity = obj.orElseThrow(() -> new EntityNotFoundException("Unable to find user with id " + id));

		return new UserDTO(entity);
	}

	@Transactional
	public UserDTO insert(UserInsertDTO dto) {
		User entity = new User();
		dtoToUser(dto, entity);
		entity.setPassword(passwordEncoder.encode(dto.getPassword()));
		entity = repository.save(entity);

		return new UserDTO(entity);
	}

	@Transactional
	public UserDTO update(Long id, UserDTO dto) {
		try {
			User entity = repository.getReferenceById(id);
			dtoToUser(dto, entity);
			entity = repository.save(entity);
			return new UserDTO(entity);
		} catch (EntityNotFoundException e) {
			if(e.getMessage() != null && e.getMessage().contains("category")) {
				throw new EntityNotFoundException(e.getMessage());
			}
			throw new EntityNotFoundException("Unable to find user with id " + id);
		}
	}

	public void delete(Long id) {
		try {
			repository.deleteById(id);
		} catch (EmptyResultDataAccessException e) {
			throw new EntityNotFoundException("Unable to find user with id " + id);
		} catch (DataIntegrityViolationException e) {
			throw new DataIntegrityViolationException("Integrity violation");
		}
	}

	private void dtoToUser(UserDTO dto, User entity) {
		entity.setFirstName(dto.getFirstName());
		entity.setLastName(dto.getLastName());
		entity.setEmail(dto.getEmail());;
		entity.getRoles().clear();
		
		dto.getRoles().forEach(roleDto -> {
			try {
				Role obj = roleRepository.getReferenceById(roleDto.getId());
				entity.getRoles().add(obj);
			} catch (EntityNotFoundException e) {
				throw new EntityNotFoundException("Unable to find category with id " + roleDto.getId());
			}
		});
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {		
		User user = repository.findByEmail(username);
		if (user == null) {
			logger.error("User not found: " + username);
			throw new UsernameNotFoundException("User not found");
		}
		
		logger.info("User found: " + username);
		return user;
	}
}
