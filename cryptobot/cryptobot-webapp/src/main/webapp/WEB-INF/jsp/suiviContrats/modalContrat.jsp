<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<!DOCTYPE html>

	<!-- Modal destinée à afficher le détail d'un contrat choisi -->
	<div class="container-xl" style="position:absolute; top:0;">
		<div class="modal fade lg" id="detailsContratModal" tabindex="-1" style="position:relative;">
			<div class="modal-dialog modal-dialog-centered">
				<div class="modal-content">
					<div class="modal-header">
						<h1 class="modal-title fs-5">
							Détail intégration contrat
						</h1>
						<button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close">
						</button>
					</div>
					<div class="modal-body">
						<jsp:include page="detailContrat.jsp" />
					</div>
					<div class="modal-footer">
						<button type="button" class="btn btn-secondary" data-bs-dismiss="modal">
							Quitter
						</button>
					</div>
				</div>
			</div>
		</div>
	</div>	