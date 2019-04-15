package me.netty;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import me.netty.nettyServer.WSServer;

@Component
public class NettyBooter implements ApplicationListener<ContextRefreshedEvent> {

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		if (event.getApplicationContext().getParent() == null) {
			try {

				WSServer.getInstance().start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		int[] n1 = {1,3};
		int[] n2 = {2};

		System.out.println(findMedianSortedArrays(n1,n2));
	}

	public static double findMedianSortedArrays(int[] nums1, int[] nums2) {

		int m = nums1.length,n = nums2.length;
		if(m > n) return findMedianSortedArrays(nums2,nums1);

		int l = 0,r = m,k = (m+n+1)/2;
		while (l <= r) {
			int i = (l + r) / 2;
			int j = k - i;
			if (i < r && nums2[j-1] > nums1[i]){
				l = i + 1; // i is too small
			}
			else if (i > l && nums1[i-1] > nums2[j]) {
				r = i - 1; // i is too big
			}
			else { // i is perfect
				int maxLeft = 0;
				if (i == 0) { maxLeft = nums2[j-1]; }
				else if (j == 0) { maxLeft = nums1[i-1]; }
				else { maxLeft = Math.max(nums1[i-1], nums2[j-1]); }
				if ( (m + n) % 2 == 1 ) { return maxLeft; }

				int minRight = 0;
				if (i == m) { minRight = nums2[j]; }
				else if (j == n) { minRight = nums1[i]; }
				else { minRight = Math.min(nums2[j], nums1[i]); }

				return (maxLeft + minRight) / 2.0;
			}
		}

		return 0.0;
	}
	
}
