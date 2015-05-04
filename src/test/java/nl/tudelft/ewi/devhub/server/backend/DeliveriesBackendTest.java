package nl.tudelft.ewi.devhub.server.backend;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import nl.tudelft.ewi.devhub.server.database.controllers.Deliveries;
import nl.tudelft.ewi.devhub.server.database.entities.Assignment;
import nl.tudelft.ewi.devhub.server.database.entities.Course;
import nl.tudelft.ewi.devhub.server.database.entities.Delivery;
import nl.tudelft.ewi.devhub.server.database.entities.Delivery.Review;
import nl.tudelft.ewi.devhub.server.database.entities.DeliveryAttachment;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.web.errors.ApiError;
import nl.tudelft.ewi.devhub.server.web.errors.UnauthorizedException;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DeliveriesBackendTest extends BackendTest {

	private static final String fileName = "myFile.txt";

	private static final String pathName = "my/fancy/path";

	@Mock
	private User currentUser;
	
	@Mock
    private Deliveries deliveriesDAO;
	
	@Mock
    private StorageBackend storageBackend;
	
	@InjectMocks
	private DeliveriesBackend deliveriesBackend;
	
	@Spy
	private Delivery delivery;
	
	@Mock
	private Assignment assignment;
	
	@Mock
	private Group group;
	
	@Mock
	private Course course;
	
	private Set<User> groupMembers;
	
	@Mock
	private InputStream in;

	@Mock
	private DeliveryAttachment attachment;
	
	@Mock
	private List<DeliveryAttachment> attachments;

	@Mock
	private Review review;
	
	private List<Delivery> deliveries;
	
	@Mock
	private File file;
	
	@Before
	public void setUp() throws IOException {
		groupMembers = Sets.newHashSet();
		groupMembers.add(currentUser);
		
		deliveries = Lists.newArrayList(delivery);
		
		when(currentUser.isAdmin()).thenReturn(true);
		when(currentUser.isAssisting(Matchers.eq(course))).thenReturn(true);
		
		when(delivery.getGroup()).thenReturn(group);
		when(delivery.getAssignment()).thenReturn(assignment);
		
		when(group.getCourse()).thenReturn(course);
		when(group.getMembers()).thenReturn(groupMembers);
		
		when(storageBackend.store(Matchers.anyString(), Matchers.eq(fileName), Matchers.eq(in))).thenReturn(pathName);
		when(storageBackend.getFile(Matchers.eq(pathName + fileName))).thenReturn(file);
		
		when(deliveriesDAO.getDeliveries(Matchers.eq(assignment), Matchers.eq(group))).thenReturn(deliveries);
		
		when(attachment.getPath()).thenReturn(pathName + fileName);
	}
	
	@Test
	public void deliveryIsStored() throws UnauthorizedException, ApiError {
		this.deliveriesBackend.deliver(delivery);
		
		verify(deliveriesDAO).persist(delivery);
	}
	
	@Test
	public void adminCanStoreDelivery() throws UnauthorizedException, ApiError {
		isAnAdmin();
		
		this.deliveriesBackend.deliver(delivery);
		
		verify(deliveriesDAO).persist(delivery);
	}

	private void isAnAdmin() {
		when(currentUser.isAssisting(Matchers.eq(course))).thenReturn(false);
		when(group.getMembers()).thenReturn(Sets.newHashSet());
	}
	
	@Test
	public void assistantCanStoreDelivery() throws UnauthorizedException, ApiError {
		isAnAssistant();
		
		this.deliveriesBackend.deliver(delivery);
		
		verify(deliveriesDAO).persist(delivery);
	}

	private void isAnAssistant() {
		when(currentUser.isAdmin()).thenReturn(false);
		when(group.getMembers()).thenReturn(Sets.newHashSet());
	}
	
	@Test
	public void groupMemberCanStoreDelivery() throws UnauthorizedException, ApiError {
		isAGroupMember();
		
		this.deliveriesBackend.deliver(delivery);
		
		verify(deliveriesDAO).persist(delivery);
	}

	private void isAGroupMember() {
		when(currentUser.isAdmin()).thenReturn(false);
		when(currentUser.isAssisting(Matchers.eq(course))).thenReturn(false);
	}
	
	@Test(expected=UnauthorizedException.class)
	public void unauthorizedWhenNoAdminNoAssistantOrNoGroupMember() throws ApiError {
		hasNoPermission();
		
		this.deliveriesBackend.deliver(delivery);
	}

	private void hasNoPermission() {
		when(currentUser.isAdmin()).thenReturn(false);
		when(currentUser.isAssisting(Matchers.eq(course))).thenReturn(false);
		when(group.getMembers()).thenReturn(Sets.newHashSet());
	}
	
	@Test(expected=IllegalStateException.class)
	public void whenAlreadyApprovedOrDisapprovedRejectDelivery() throws UnauthorizedException, ApiError {
		when(deliveriesDAO.lastDeliveryIsApprovedOrDisapproved(Matchers.eq(assignment), Matchers.eq(group)))
			.thenReturn(true);
		
		this.deliveriesBackend.deliver(delivery);
	}
	
	@SuppressWarnings("unchecked")
	@Test(expected=ApiError.class)
	public void whenStoringDeliveryFailedErrorToUser() throws UnauthorizedException, ApiError {
		when(deliveriesDAO.persist(Matchers.eq(delivery))).thenThrow(Exception.class);
		
		this.deliveriesBackend.deliver(delivery);
	}
	
	@Test
	public void attachFileToDelivery() throws UnauthorizedException, ApiError {
		attachedFile();
	}

	private void attachedFile() throws ApiError {
		this.deliveriesBackend.attach(delivery, fileName, in);
		
		assertEquals(1, delivery.getAttachments().size());
		// Because a simple get did not return the correct deliveryAttachment, we had to do it the fancy way.
		assertEquals(1, delivery.getAttachments().stream()
				.map((attachment) ->
					attachment.getFileName().equals(fileName)
				).count());
	}
	
	@Test
	public void attachFileToDeliveryWithAttachments() throws UnauthorizedException, ApiError {
		delivery.setAttachments(attachments);
		this.deliveriesBackend.attach(delivery, fileName, in);
		
		verify(attachments).add(Matchers.argThat(new BaseMatcher<DeliveryAttachment>() {

			@Override
			public boolean matches(Object arg0) {
				return ((DeliveryAttachment) arg0).getDelivery().equals(delivery);
			}

			@Override
			public void describeTo(Description arg0) {
				
			}

			
		}));
	}
	
	@Test
	public void adminCanAttachDelivery() throws UnauthorizedException, ApiError {
		isAnAdmin();
		
		attachedFile();
	}

	@Test
	public void assistantCanAttachDelivery() throws UnauthorizedException, ApiError {
		isAnAssistant();
		
		attachedFile();
	}

	@Test
	public void groupMemberCanAttachDelivery() throws UnauthorizedException, ApiError {
		isAGroupMember();
		
		attachedFile();
	}

	@Test(expected=UnauthorizedException.class)
	public void unauthorizedWhenNoAdminNoAssistantOrNoGroupMemberAttachDelivery() throws ApiError {
		hasNoPermission();
		
		attachedFile();
	}
	
	@SuppressWarnings("unchecked")
	@Test(expected=ApiError.class)
	public void whenAttachingDeliveryFailedErrorToUser() throws UnauthorizedException, ApiError {
		when(deliveriesDAO.merge(Matchers.eq(delivery))).thenThrow(Exception.class);
		
		attachedFile();
	}

	@Test
	public void reviewDelivery() throws UnauthorizedException, ApiError {
		this.deliveriesBackend.review(delivery, review);
		
		verify(delivery).setReview(Matchers.eq(review));
	}
	
	@Test
	public void adminCanReviewDelivery() throws UnauthorizedException, ApiError {
		isAnAdmin();
		
		reviewDelivery();
	}

	@Test
	public void assistantCanReviewDelivery() throws UnauthorizedException, ApiError {
		isAnAssistant();
		
		reviewDelivery();
	}

	@Test(expected=UnauthorizedException.class)
	public void groupMemberCanNotReviewDelivery() throws UnauthorizedException, ApiError {
		isAGroupMember();
		
		reviewDelivery();
	}

	@Test(expected=UnauthorizedException.class)
	public void unauthorizedWhenNoAdminNoAssistantOrNoGroupMemberReviewDelivery() throws ApiError {
		hasNoPermission();
		
		reviewDelivery();
	}
	
	@SuppressWarnings("unchecked")
	@Test(expected=ApiError.class)
	public void whenReviewingDeliveryFailedErrorToUser() throws UnauthorizedException, ApiError {
		when(deliveriesDAO.merge(Matchers.eq(delivery))).thenThrow(Exception.class);
		
		reviewDelivery();
	}
	
	@Test
	public void getAttachment() {
		List<DeliveryAttachment> attachments = Lists.newArrayList(attachment);
		
		when(delivery.getAttachments()).thenReturn(attachments);
		
		assertEquals(file, this.deliveriesBackend.getAttachment(assignment, group, pathName + fileName));
	}
	
	@Test
	public void adminCanGetAttachement() throws UnauthorizedException, ApiError {
		isAnAdmin();
		
		getAttachment();
	}

	@Test
	public void assistantCanGetAttachement() throws UnauthorizedException, ApiError {
		isAnAssistant();
		
		getAttachment();
	}

	@Test
	public void groupMemberCanGetAttachement() throws UnauthorizedException, ApiError {
		isAGroupMember();
		
		getAttachment();
	}

	@Test(expected=UnauthorizedException.class)
	public void unauthorizedWhenNoAdminNoAssistantOrNoGroupMemberGetAttachement() throws ApiError {
		hasNoPermission();
		
		getAttachment();
	}
	
	@Test(expected=UnauthorizedException.class)
	public void unauthorizedWhenFetchingFromOtherGroup() throws ApiError {
		List<DeliveryAttachment> attachments = Lists.newArrayList(attachment);
		
		when(delivery.getAttachments()).thenReturn(attachments);
		when(attachment.getPath()).thenReturn("bogusPath.txt");
		
		this.deliveriesBackend.getAttachment(assignment, group, pathName + fileName);
	}
	
	@Test
	public void assignmentStatsFromLatestDeliveries() {
		when(assignment.getCourse()).thenReturn(course);
		when(deliveriesDAO.getLastDeliveries(Matchers.eq(assignment))).thenReturn(deliveries);
		
		assertNotNull(this.deliveriesBackend.getAssignmentStats(assignment));
	}

}
